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
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.DefaultQuickServer;
import net.apisp.quick.server.MappingResolver;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.ServerContext;

/**
 * 框架帮助类
 * 
 * @author UJUED
 * @date 2018-06-08 10:34:37
 */
public class Quick {
    private static final Logger LOGGER = Logger.get(Quick.class);
    private static final ServerContext SERVER_CONTEXT = ServerContext.instance();
    private static QuickServer server = choseServer(SERVER_CONTEXT);

    private static QuickServer choseServer(ServerContext serverContext) {
        if (server == null) {
            Class<QuickServer> serverClass = serverContext.getServerClass();
            if (Objects.nonNull(serverClass)) {
                try {
                    server = serverClass.newInstance();
                    LOGGER.info("The server %s chosed.", serverClass);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.error("自定义QuickServer需要有无参构造函数");
                    server = new DefaultQuickServer();
                }
            } else {
                server = new DefaultQuickServer();
                LOGGER.info("The settings error, default server instance chosed.");
            }
        }
        return server;
    }

    /**
     * Server设置上下文 -> 启动
     */
    private static void startServer() {
        server.setContext(SERVER_CONTEXT);
        server.start();
    }

    /**
     * 启动QuickServer
     * 
     * @param classes
     *            包含URI与逻辑映射的类
     * @return
     */
    public static ServerContext run(Class<?> mainClass, String ...args) {
        Configuration.applySystemArgs(args);
        MappingResolver.prepare(mainClass, SERVER_CONTEXT).resolve();
        startServer();
        return SERVER_CONTEXT;
    }
}
