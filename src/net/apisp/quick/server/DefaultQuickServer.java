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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.apisp.quick.log.Logger;
import net.apisp.quick.server.var.ServerContext;

/**
 * @author UJUED
 * @date 2018-06-12 15:57:49
 */
public class DefaultQuickServer extends QuickServer {
    private static final Logger LOGGER = Logger.get(DefaultQuickServer.class);
    private static boolean shouldRunning = true;
    private List<SocketAutonomy> keepList = new ArrayList<>(100);

    @Override
    public void run(ServerContext serverContext) throws Exception {
        QuickServerMonitor.work(keepList);
        ServerSocket ss = new ServerSocket(serverContext.port());
        while (shouldRunning) {
            Socket sock = ss.accept();
            LOGGER.debug("New connection come in.");
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
        private static final Logger LOGGER = Logger.get(QuickServerMonitor.class);
        public static final int SOCKET_MAX_FREE_TIME = 1000 * 6;
        private List<SocketAutonomy> keepList;

        public QuickServerMonitor(List<SocketAutonomy> keepList) {
            this.keepList = keepList;
        }

        public static void work(List<SocketAutonomy> keepList) {
            QuickServerMonitor monitor = new QuickServerMonitor(keepList);
            monitor.setPriority(Thread.MIN_PRIORITY);
            monitor.setDaemon(true);
            monitor.start();
        }

        @Override
        public void run() {
            try {
                while (!this.isInterrupted()) {
                    for (Iterator<SocketAutonomy> iter = keepList.iterator(); iter.hasNext();) {
                        SocketAutonomy so = iter.next();
                        LOGGER.debug("%s free time is %ds", so, so.freeTime() / 1000);
                        if (so.freeTime() > SOCKET_MAX_FREE_TIME) {
                            so.interrupt();
                            try {
                                so.close();
                            } catch (IOException e) {
                            }
                            iter.remove();
                            LOGGER.debug("%s is timeout. Closed.", so);
                        }
                    }
                    Thread.sleep(1000 * 6);
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
