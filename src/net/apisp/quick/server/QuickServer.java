/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.IOException;
import java.util.Objects;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.ServerContext;
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.low.DefaultServer;
import net.apisp.quick.util.Safes;

/**
 * 抽象的QuickServer标准
 * 
 * @author UJUED
 * @date 2018-06-08 10:33:31
 */
public abstract class QuickServer {
    public static final Logger LOGGER = Logger.get(QuickServer.class);
    private ServerContext serverContext;
    private static QuickServer server;
    private volatile boolean afterCanRun = true;

    /**
     * 选择一个Server实现
     *
     * @return
     */
    public static synchronized QuickServer chose() {
        if (server == null) {
            Class<QuickServer> serverClass = Safes.loadClass(Configuration.get("server").toString(),
                    QuickServer.class);
            if (Objects.nonNull(serverClass)) {
                try {
                    server = serverClass.newInstance();
                    LOGGER.info("The server %s chosed.", serverClass);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.error("自定义QuickServer需要有无参构造函数");
                    server = new DefaultServer();
                }
            } else {
                server = new DefaultServer();
                LOGGER.info("The settings error, default server instance chosed.");
            }
        }
        return server;
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                try {
                    server.run(serverContext);
                } catch (IOException e) {
                    LOGGER.error("Server start error, IO Exception occered.");
                } catch (Exception e) {
                    LOGGER.error("Server start error, Unkonwn Exception occered.");
                }
                afterCanRun = false;
            }
        }.start();
        try {
            Thread.sleep(100);
            if (afterCanRun) {
                server.afterRunning(serverContext);
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            LOGGER.error("Server start success, but after occered.");
        }

    }

    public void setContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public abstract void run(ServerContext context) throws Exception;

    public void afterRunning(ServerContext context) throws Exception {
    }
}
