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
package net.apisp.quick.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.thread.TaskExecutor;

/**
 * @author UJUED
 * @date 2018-06-12 15:57:49
 */
public class DefaultQuickServer extends QuickServer {
    private static final Log LOG = LogFactory.getLog(DefaultQuickServer.class);
    private static boolean shouldRunning = true;
    private static int processors = Runtime.getRuntime().availableProcessors();
    public static final TaskExecutor socketAutonomyExecutor = TaskExecutor.create("socket", processors * 3 * 20);
    public static final TaskExecutor responseExecutor = TaskExecutor.create("response", processors * 3);

    private List<SocketAutonomy> keepList = new ArrayList<>(100);

    @Override
    public void run(ServerContext serverContext) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ServerContext.tryGet().executor().shutdown();
                DefaultQuickServer.socketAutonomyExecutor.shutdown();
                DefaultQuickServer.responseExecutor.shutdown();
            }
        });
        QuickServerMonitor.start(keepList);
        ServerSocket ss = new ServerSocket(serverContext.port());
        while (shouldRunning) {
            Socket sock = ss.accept();
            LOG.debug("New connection come in.");
            SocketAutonomy.activeAsync(sock, keepList);
        }
        ss.close();
    }

    /**
     * HTTP/1.1 请求长连接定时清理
     * 
     * @author UJUED
     * @date 2018-06-12 18:18:49
     */
    static class QuickServerMonitor extends Thread {
        public static final int SOCKET_MAX_FREE_TIME = 1000 * 60;
        private List<SocketAutonomy> keepList;

        public QuickServerMonitor(List<SocketAutonomy> keepList) {
            this.keepList = keepList;
        }

        public static void start(List<SocketAutonomy> keepList) {
            QuickServerMonitor monitor = new QuickServerMonitor(keepList);
            monitor.setPriority(Thread.MIN_PRIORITY);
            monitor.setDaemon(true);
            monitor.setName("monitor");
            monitor.start();
        }

        @Override
        public void run() {
            try {
                while (!this.isInterrupted()) {
                    for (Iterator<SocketAutonomy> iter = keepList.iterator(); iter.hasNext();) {
                        SocketAutonomy sa = iter.next();
                        LOG.debug("{} free time is {}s", sa, sa.freeTime() / 1000);
                        if (sa.freeTime() > SOCKET_MAX_FREE_TIME) {
                            sa.interrupt();
                            sa.close();
                            iter.remove();
                            LOG.debug("{} is timeout. Closed.", sa);
                        }
                    }
                    Thread.sleep(1000 * 20);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
