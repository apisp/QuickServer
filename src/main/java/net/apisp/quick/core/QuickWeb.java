package net.apisp.quick.core;

import net.apisp.quick.core.criterion.QuickServer;
import net.apisp.quick.core.criterion.http.WebApplicationContext;
import net.apisp.quick.core.criterion.http.WebApplicationContextEnhancer;
import net.apisp.quick.core.criterion.http.annotation.EnableCros;
import net.apisp.quick.core.criterion.http.annotation.Scanning;
import net.apisp.quick.core.criterion.ioc.ClassScanner;
import net.apisp.quick.core.criterion.ioc.annotation.Controller;
import net.apisp.quick.core.criterion.ioc.annotation.Factory;
import net.apisp.quick.core.criterion.ioc.annotation.Singleton;
import net.apisp.quick.core.exception.StopServerException;
import net.apisp.quick.core.standard.http.StandardApplicationContext;
import net.apisp.quick.core.standard.ioc.FactoryResolver;
import net.apisp.quick.core.standard.ioc.Injections;
import net.apisp.quick.core.standard.ioc.SimpleClassScanner;
import net.apisp.quick.core.standard.ioc.SingletonRegister;
import net.apisp.quick.old.server.MappingResolver;
import net.apisp.quick.old.server.std.ContextEnhancer;
import net.apisp.quick.util.Quicks;
import net.apisp.quick.util.Reflects;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Objects;

import static net.apisp.quick.old.server.http.ResponseExecutor.LOG;

public class QuickWeb implements QuickServer<WebApplicationContext> {

    private Class<?> bootClass;

    private QuickWeb() throws StopServerException {
        this.bootClass = getBootClass();
    }

    @Override
    public WebApplicationContext boot(Object... args) {
        WebApplicationContext webApplicationContext = new StandardApplicationContext();
        return webApplicationContext;
    }

    private URI getUserBinDir() {
        String url = bootClass.getResource("/" + bootClass.getSimpleName().replace('.', '/') + ".class").toString();
        return URI.create(url.substring(0, url.length() - bootClass.getSimpleName().length() - 6));
    }

    private URI getSupportClassDir() {
        String url = this.getClass().getResource(this.getClass().getSimpleName() + ".class").toString();
        return URI.create(url.substring(0, url.length() - this.getClass().getName().length() - 6));
    }

    private String bootClassPackageName() {
        if (Objects.isNull(bootClass) || bootClass.getName().indexOf('.') == -1) {
            return "";
        }
        return bootClass.getName().substring(0, bootClass.getName().lastIndexOf('.'));
    }

    private void prepareWebServer(WebApplicationContext webApplicationContext) {

        // 用户Bin目录
        URI userBinDir = getUserBinDir();
        webApplicationContext.container().accept("user.classpath.uri", userBinDir);

        // 扫描Factories并缓存制造的对象
        ClassScanner classScanner = SimpleClassScanner.create(userBinDir, bootClassPackageName());
        FactoryResolver.prepare(classScanner.getByAnnotation(Factory.class), webApplicationContext.container()).resolve();

        // QuickServer support
        URI supportClassDir = getSupportClassDir();
        boolean shouldScanningSupport = !supportClassDir.toString().equals(userBinDir.toString());
        ClassScanner supportClassScanner = SimpleClassScanner.create(supportClassDir, "net.apisp.quick.support");
        if (shouldScanningSupport) {
            FactoryResolver.prepare(supportClassScanner.getByAnnotation(Factory.class),
                    webApplicationContext.container()).resolve();
            SingletonRegister.prepare(supportClassScanner.getByAnnotation(Singleton.class),
                    webApplicationContext.container()).register();
        }

        // 增强 WebApplicationContext
        Class<?>[] preparationClasses = classScanner.getByInterface(WebApplicationContextEnhancer.class);
        for (Class<?> preparationClass : preparationClasses) {
            try {
                WebApplicationContextEnhancer preparation = (WebApplicationContextEnhancer) preparationClass.getConstructor().newInstance();
                Injections.inject(preparation, webApplicationContext.container());
                preparation.enhance(webApplicationContext);
                LOG.info("{} prepared.", preparationClass.getName());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOG.warn("{} is not suitable.", preparationClass);
            }
        }

        // 扫描标注了Singleton注解的单例并缓存
        SingletonRegister.prepare(classScanner.getByAnnotation(Singleton.class), webApplicationContext.container()).register();

        // 解决URI的映射关系

        // 扫描到的
        Class<?>[] annotationControllerClasses = classScanner.getByAnnotation(Controller.class);
        MappingResolver resolver = webApplicationContext.container().accept(MappingResolver.prepare(annotationControllerClasses));
        // 启动类指定的类
        Scanning scanning = bootClass.getAnnotation(Scanning.class);
        if (Objects.nonNull(scanning)) {
            resolver.addControllerClasses(scanning.value()).addControllerClasses(bootClass);
        }
        if (shouldScanningSupport) {
            Class<?>[] supportControllerClasses = supportClassScanner.getByAnnotation(Controller.class);
            resolver.addControllerClasses(supportControllerClasses);
        }
        resolver.resolveTo(null);

        // TODO Mapping Resolver
    }

    private Class<?> getBootClass() throws StopServerException {
        try {
            String bootClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            return this.getClass().getClassLoader().loadClass(bootClassName);
        } catch (ClassNotFoundException e) {
            throw new StopServerException("不存在启动类");
        }
    }
}
