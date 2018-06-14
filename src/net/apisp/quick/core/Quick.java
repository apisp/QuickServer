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
    private Class<?> bootClass;
    private QuickServer server;
    private ServerContext serverContext;
    private String[] bootArgs = new String[0];

    public Quick() {
    }

    public Quick(String[] bootArgs) {
        this(null, bootArgs);
    }

    public Quick(Class<?> bootClass) {
        this(bootClass, new String[0]);
    }

    public Quick(Class<?> bootClass, String[] bootArgs) {
        if (bootClass == null) {
            try {
                bootClass = Quick.class.getClassLoader()
                        .loadClass(Thread.currentThread().getStackTrace()[2].getClassName());
            } catch (ClassNotFoundException e) {
            }
        }
        this.bootClass = bootClass;
        this.bootArgs = bootArgs;
        this.serverContext = ServerContext.init();
        this.server = choseServer(serverContext);
    }

    /**
     * Server设置上下文 -> 启动
     */
    private void startServer(ServerContext serverContext) {
        server.setContext(serverContext);
        server.addEvent((args) -> {
            // TODO @Factory 缓存单例对象
        });
        server.start();
    }

    @Override
    public ServerContext boot() {
        Configuration.applySystemArgs(bootArgs);
        MappingResolver.prepare(bootClass, serverContext).resolve();
        startServer(serverContext);
        return serverContext;
    }

    public static ServerContext boot(Class<?> bootClass, String... args) {
        return new Quick(bootClass, args).boot();
    }

    /**
     * 使用main函数所在类作为bootClass
     */
    public static ServerContext boot(String... args) {
        return new Quick(args).boot();
    }

    /**
     * 选择合适的QuickServer
     * 
     * @param serverContext
     * @return
     */
    private static synchronized QuickServer choseServer(ServerContext serverContext) {
        QuickServer quickServer = null;
        Class<QuickServer> serverClass = serverContext.serverClass();
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

}
