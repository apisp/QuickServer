/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.IOException;
import java.net.BindException;

import net.apisp.quick.core.type.QuickServerRunner;
import net.apisp.quick.log.Logger;

/**
 * 抽象的QuickServer标准
 * 
 * @author UJUED
 * @date 2018-06-08 10:33:31
 */
public abstract class QuickServer {
    public static final Logger LOGGER = Logger.get(QuickServer.class);
    private ServerContext serverContext;

    public void start() {
        new QuickServerThread(serverContext, (context) -> {
            run(context);
        }).startAndDoAfterRunning((context) -> {
            afterRunning(context);
        });
    }

    public void setContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public abstract void run(ServerContext serverContext) throws Exception;

    public void afterRunning(ServerContext serverContext) throws Exception {
    }
}

class QuickServerThread extends Thread {
    private ServerContext serverContext;
    private QuickServerRunner runner;
    private static final Logger LOGGER = Logger.get(QuickServerThread.class);

    public QuickServerThread(ServerContext serverContext, QuickServerRunner runner) {
        this.serverContext = serverContext;
        this.runner = runner;
    }

    @Override
    public void run() {
        try {
            runner.run(serverContext);
        } catch (BindException e) {
            LOGGER.error("The port %d already inuse.", serverContext.port());
        } catch (IOException e) {
            LOGGER.error("Server start error, IO Exception occered.");
        } catch (Exception e) {
            LOGGER.error("Server start error, Unkonwn Exception occered. %s", e);
            e.printStackTrace();
        }
        serverContext.setNormative(false);
    }

    public void startAndDoAfterRunning(QuickServerRunner runner) {
        this.start();
        try {
            Thread.sleep(100);
            if (serverContext.isNormative()) {
                LOGGER.show("Started Quick API Server on port (%s)", serverContext.port());
                runner.run(serverContext);
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            LOGGER.error("Server start success, but after Exception occered.");
        }
    }
}
