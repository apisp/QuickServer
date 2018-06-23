/**
 * Copyright 2018-present, APISP.NET.
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
public class Quick implements Bootable<ServerContext> {
    private static final Log LOG = LogFactory.getLog(Quick.class);
    private Class<?> bootClass;
    private QuickServer server;
    private ServerContext serverContext;
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
        this.serverContext = ServerContext.init();
        this.server = Quick.newServer(serverContext.serverClass());
    }

    private void prepareServer() {
        // 单例缓存
        SimpleClassScanner classScanner = SimpleClassScanner.create(userBin, Quicks.packageName(bootClass.getName()));
        Class<?>[] factories = classScanner.getByAnnotation(Factory.class);
        Class<?>[] singletons = classScanner.getByAnnotation(Singleton.class);
        SingletonRegister singletonRegister = new SingletonRegister();
        singletonRegister.classes(singletons).factories(factories).cache(serverContext);

        // QuickServer support
        String supportPackage = "/net/apisp/quick/support/";
        String url = this.getClass().getResource(supportPackage).toString();
        URI uri = URI.create(url.substring(0, url.length() - supportPackage.length()));
        SimpleClassScanner supportClassScanner = SimpleClassScanner.create(uri, "net.apisp.quick.support");
        singletonRegister.factories(supportClassScanner.getByAnnotation(Factory.class)).cache(serverContext);

        // 解决URI的映射关系
        Class<?>[] controllerClss = classScanner.getByAnnotation(Controller.class);
        Class<?>[] supportControllerClss = supportClassScanner.getByAnnotation(Controller.class);
        MappingResolver.prepare(bootClass, serverContext).addControllerClasses(controllerClss)
                .addControllerClasses(supportControllerClss).resolve();
    }

    /**
     * Server设置上下文 -> 启动
     */
    private void startServer() {
        server.setContext(serverContext);
        server.start();
    }

    @Override
    public ServerContext boot() {
        Configuration.applySystemArgs(bootArgs);
        prepareServer();
        startServer();
        return serverContext;
    }

    public static ServerContext boot(Class<?> bootClass, String... args) {
        return new Quick(bootClass, args).boot();
    }

    /**
     * 使用main函数所在类作为bootClass
     */
    public static ServerContext boot(String... args) {
        return new Quick(null, 3, args).boot();
    }

    /**
     * 选择合适的QuickServer
     * 
     * @param serverContext
     * @return
     */
    private static synchronized QuickServer newServer(Class<QuickServer> serverClass) {
        QuickServer quickServer = null;
        if (Objects.nonNull(serverClass)) {
            try {
                quickServer = serverClass.newInstance();
                LOG.info("The server %s hit.", serverClass);
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
