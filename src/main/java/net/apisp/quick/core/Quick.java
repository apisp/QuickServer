/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.core;

import java.net.URI;
import java.util.Objects;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.ioc.ClassScanner;
import net.apisp.quick.ioc.Container.Injections;
import net.apisp.quick.ioc.FactoryResolver;
import net.apisp.quick.ioc.SimpleClassScanner;
import net.apisp.quick.ioc.SingletonRegister;
import net.apisp.quick.ioc.annotation.Controller;
import net.apisp.quick.ioc.annotation.Factory;
import net.apisp.quick.ioc.annotation.Singleton;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.DefaultQuickServer;
import net.apisp.quick.server.MappingResolver;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.thread.Task;
import net.apisp.quick.util.Quicks;

/**
 * 框架帮助类
 * 
 * @author UJUED
 * @date 2018-06-08 10:34:37
 */
public class Quick implements Bootable<QuickContext> {
    private static final Log LOG = LogFactory.getLog(Quick.class);
    private Class<?> bootClass;
    private QuickServer server;
    private QuickContext quickContext;
    private String[] bootArgs = new String[0];
    private URI userBin;

    public Quick() {
        this(null, new String[0]);
    }

    public Quick(String[] bootArgs) {
        this(null, bootArgs);
    }

    public Quick(Class<?> bootClass) {
        this(bootClass, new String[0]);
    }

    public Quick(Class<?> bootClass, String[] bootArgs) {
        this(bootClass, 2, bootArgs);
    }

    private Quick(Class<?> bootClass, int index, String[] bootArgs) {
        if (bootClass == null) {
            try {
                String bootClassName = Thread.currentThread().getStackTrace()[index].getClassName();
                bootClass = Quick.class.getClassLoader().loadClass(bootClassName);
                String url = bootClass.getResource("/" + bootClassName.replace('.', '/') + ".class").toString();
                userBin = URI.create(url.substring(0, url.length() - bootClassName.length() - 6));
            } catch (ClassNotFoundException e) {
            }
        }
        this.bootClass = bootClass;
        this.bootArgs = bootArgs;
    }

    private void initServer() {
        Configuration.applySystemArgs(bootArgs);
        this.quickContext = ServerContext.init();
        this.quickContext.accept("quickServer.bootClass", bootClass);
        this.server = Quick.newServer(quickContext.serverClass());
    }

    private void prepareServer() {
        // 扫描Factories并缓存制造的对象
        ClassScanner classScanner = SimpleClassScanner.create(userBin, Quicks.packageName(bootClass));
        quickContext.accept("user.classpath.uri", userBin);
        FactoryResolver.prepare(classScanner.getByAnnotation(Factory.class), quickContext).resolve();

        // QuickServer support
        String url = this.getClass().getResource(this.getClass().getSimpleName() + ".class").toString();
        URI uri = URI.create(url.substring(0, url.length() - this.getClass().getName().length() - 6));
        boolean shouldScanningSupport = !uri.toString().equals(userBin.toString());
        ClassScanner supportClassScanner = SimpleClassScanner.create(uri, "net.apisp.quick.support");
        if (shouldScanningSupport) {
            FactoryResolver.prepare(supportClassScanner.getByAnnotation(Factory.class), quickContext).resolve();
            SingletonRegister.prepare(supportClassScanner.getByAnnotation(Singleton.class), quickContext).register();
        }

        // 增强Context
        Class<?>[] preparations = classScanner.getByInterface(ContextEnhancer.class);
        for (int i = 0; i < preparations.length; i++) {
            try {
                ContextEnhancer preparation = (ContextEnhancer) preparations[i].newInstance();
                Injections.inject(preparation, quickContext);
                preparation.enhance(quickContext);
                LOG.info("{} preapared.", preparations[i].getName());
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("{} is not suitable.", preparations[i]);
            }
        }

        // 扫描标注了Singleton注解的单例并缓存
        SingletonRegister.prepare(classScanner.getByAnnotation(Singleton.class), quickContext).register();

        // 解决URI的映射关系
        MappingResolver resolver = MappingResolver.prepare(bootClass, quickContext);
        quickContext.accept(resolver);
        Class<?>[] controllerClss = classScanner.getByAnnotation(Controller.class);
        resolver.addControllerClasses(controllerClss);
        if (shouldScanningSupport) {
            Class<?>[] supportControllerClss = supportClassScanner.getByAnnotation(Controller.class);
            resolver.addControllerClasses(supportControllerClss);
        }
        resolver.resolve();
    }

    /**
     * Server设置上下文 -> 启动
     */
    private void startServer() {
        server.setContext(quickContext);
        server.start();
    }

    @Override
    public QuickContext boot() {
        initServer();
        prepareServer();
        startServer();
        return quickContext;
    }

    public static QuickContext boot(Class<?> bootClass, String... args) {
        return new Quick(bootClass, args).boot();
    }

    /**
     * 使用main函数所在类作为bootClass
     */
    public static QuickContext boot(String... args) {
        return new Quick(null, 3, args).boot();
    }

    /**
     * 选择合适的QuickServer
     * 
     * @param quickContext
     * @return
     */
    private static synchronized QuickServer newServer(Class<QuickServer> serverClass) {
        QuickServer quickServer = null;
        if (Objects.nonNull(serverClass)) {
            try {
                quickServer = serverClass.newInstance();
                LOG.info("The server {} hit.", serverClass);
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("Who extends QuickServer need the non-args' constructor. Default server instance hit.");
                quickServer = new DefaultQuickServer();
            }
        } else {
            quickServer = new DefaultQuickServer();
            LOG.info("The settings error! Default server instance hit.");
        }
        return quickServer;
    }

    /**
     * 添加些事件，在Server启动前会按添加顺序执行
     * 
     * @param event
     * @param args
     */
    public void addEvent(Task event, Object... args) {
        server.addEvent(event, args);
    }

}
