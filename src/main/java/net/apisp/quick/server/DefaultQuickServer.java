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
import java.util.Iterator;
import java.util.List;

import net.apisp.quick.core.QuickContext;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.thread.TaskExecutor;

/**
 * 默认的QuickServer实现，支持长连接，理论高并发
 * 
 * @author Ujued
 * @date 2018-06-12 15:57:49
 * @see SocketAutonomy
 */
public class DefaultQuickServer extends QuickServer {
    private static final Log LOG = LogFactory.getLog(DefaultQuickServer.class);
    public static final int MAX_RESPONSE_CONCURRET_COUNT = Runtime.getRuntime().availableProcessors() * 3 + 1;
    public static final int MAX_SOCKET_KEEP_COUNT = MAX_RESPONSE_CONCURRET_COUNT * 10;
    public static final TaskExecutor SOCKET_AUTONOMY_EXECUTOR = TaskExecutor.create("socket", MAX_SOCKET_KEEP_COUNT);
    public static final TaskExecutor RESPONSE_EXECUTOR = TaskExecutor.create("response", MAX_RESPONSE_CONCURRET_COUNT);

    @Override
    public void run(QuickContext serverContext) throws Exception {
        QuickServerMonitor.start(SocketAutonomy.SOCKET_KEEP_LIST);
        ServerSocket serverSocket = new ServerSocket(serverContext.port());
        while (super.shouldRunning()) {
            Socket sock = serverSocket.accept();
            LOG.debug("New connection come in.");
            SocketAutonomy.activeAsync(sock);
        }
        serverSocket.close();
    }

    @Override
    protected void onShutdown(QuickContext serverContext) throws Exception {
        ServerContext.tryGet().executor().shutdown();
        DefaultQuickServer.SOCKET_AUTONOMY_EXECUTOR.shutdown();
        DefaultQuickServer.RESPONSE_EXECUTOR.shutdown();
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
