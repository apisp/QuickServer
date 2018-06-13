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
package net.apisp.quick.server;

import java.io.IOException;
import java.net.BindException;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.var.ServerContext;

/**
 * QuickServer标准规范
 * 
 * @author UJUED
 * @date 2018-06-08 10:33:31
 */
public abstract class QuickServer {
    public static final Log LOG = LogFactory.getLog(QuickServer.class);
    private ServerContext serverContext;

    public final void start() {
        new QuickServerThread(serverContext, (context) -> {
            run(context);
        }).startAndDoAfterRunning((context) -> {
            afterRunning(context);
        });
    }

    public final void setContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public abstract void run(ServerContext serverContext) throws Exception;

    protected void afterRunning(ServerContext serverContext) throws Exception {
    }

    class QuickServerThread extends Thread {
        private ServerContext serverContext;
        private QuickServerRunner runner;

        public QuickServerThread(ServerContext serverContext, QuickServerRunner runner) {
            this.serverContext = serverContext;
            this.runner = runner;
        }

        @Override
        public void run() {
            try {
                runner.run(serverContext);
            } catch (BindException e) {
                serverContext.setNormative(false);
                LOG.error("The port %d already inuse.", serverContext.port());
            } catch (IOException e) {
                serverContext.setNormative(false);
                LOG.error("Server start error, IO Exception occered.");
            } catch (Exception e) {
                serverContext.setNormative(false);
                LOG.error("Server start error, Unkonwn Exception occered. %s", e);
                e.printStackTrace();
            }
        }

        public void startAndDoAfterRunning(QuickServerRunner runner) {
            this.start();
            try {
                Thread.sleep(100);
                if (serverContext.isNormative()) {
                    LOG.show("Started Quick API Server on port (%s)", serverContext.port());
                    runner.run(serverContext);
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                LOG.error("Server start success, but after Exception occered.");
            }
        }
    }

    @FunctionalInterface
    interface QuickServerRunner {
        void run(ServerContext serverContext) throws Exception;
    }
}
