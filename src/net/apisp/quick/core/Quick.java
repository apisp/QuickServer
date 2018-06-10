/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import java.util.Objects;

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
    private static final ServerContext SERVER_CONTEXT = ServerContext.tryGet();
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
    public static ServerContext run(Class<?>... classes) {
        MappingResolver.prepare(classes, SERVER_CONTEXT).resolve();
        startServer();
        LOGGER.show("Started Quick API Server on port (%s)", SERVER_CONTEXT.port());
        return SERVER_CONTEXT;
    }
}
