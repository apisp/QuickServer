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

import java.util.Objects;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.DefaultQuickServer;
import net.apisp.quick.server.MappingResolver;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.var.ServerContext;

/**
 * 框架帮助类
 * 
 * @author UJUED
 * @date 2018-06-08 10:34:37
 */
public class Quick implements Bootable<ServerContext> {
    private static final Log LOG = LogFactory.getLog(Quick.class);
    private static QuickServer server;
    private Class<?> bootClass;
    private String[] bootArgs = new String[0];

    public Quick() {
    }

    public Quick(String[] bootArgs) {
        this.bootArgs = bootArgs;
    }

    public Quick(Class<?> bootClass) {
        this.bootClass = bootClass;
    }

    public Quick(Class<?> bootClass, String[] bootArgs) {
        this.bootClass = bootClass;
        this.bootArgs = bootArgs;
    }

    @Override
    public ServerContext boot() {
        ServerContext serverContext = null;
        if (this.bootClass == null) {
            serverContext = Quick.boot(bootArgs);
        }
        serverContext = Quick.boot(this.bootClass, bootArgs);
        return serverContext;
    }

    /**
     * 启动QuickServer
     * 
     * @param classes
     *            包含URI与逻辑映射的类
     * @return
     */
    public static ServerContext boot(Class<?> bootClass, String... args) {
        Configuration.applySystemArgs(args);
        ServerContext serverContext = ServerContext.init();
        MappingResolver.prepare(bootClass, serverContext).resolve();
        server = choseServer(serverContext);
        startServer(serverContext);
        return serverContext;
    }

    /**
     * 使用main函数所在类作为bootClass
     */
    public static ServerContext boot(String... args) {
        Class<?> bootClass = null;
        try {
            bootClass = Quick.class.getClassLoader()
                    .loadClass(Thread.currentThread().getStackTrace()[2].getClassName());
        } catch (ClassNotFoundException e) {
        }
        return boot(bootClass, args);
    }

    /**
     * 选择合适的QuickServer
     * 
     * @param serverContext
     * @return
     */
    private static synchronized QuickServer choseServer(ServerContext serverContext) {
        if (server == null) {
            Class<QuickServer> serverClass = serverContext.getServerClass();
            if (Objects.nonNull(serverClass)) {
                try {
                    server = serverClass.newInstance();
                    LOG.info("The server %s hit.", serverClass);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOG.warn("Who extends QuickServer need the non-args' constructor. Default server instance hit.");
                    server = new DefaultQuickServer();
                }
            } else {
                server = new DefaultQuickServer();
                LOG.info("The settings error! Default server instance hit.");
            }
        }
        return server;
    }

    /**
     * Server设置上下文 -> 启动
     */
    private static void startServer(ServerContext serverContext) {
        server.setContext(serverContext);
        server.start();
    }
}
