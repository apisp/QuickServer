/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.server.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.std.QuickContext;
import net.apisp.quick.server.std.QuickServer;
import net.apisp.quick.server.ServerContext;
import net.apisp.quick.thread.TaskExecutor;

/**
 * 默认的QuickServer实现，支持长连接，理论高并发
 *
 * @author ujued
 * @see SocketAutonomy
 */
public class DefaultQuickServer extends QuickServer {
    private static final Log LOG = LogFactory.getLog(DefaultQuickServer.class);
    /**
     * 最大并发响应数
     */
    public static final int MAX_RESPONSE_CONCURRENT_COUNT = Runtime.getRuntime().availableProcessors() * 3 + 1;
    /**
     * 最大长链接数
     */
    public static final int MAX_SOCKET_KEEP_COUNT = MAX_RESPONSE_CONCURRENT_COUNT * 10;
    /**
     * 请求处理线程池
     */
    public static final TaskExecutor SOCKET_AUTONOMY_EXECUTOR = TaskExecutor.create("socket", MAX_SOCKET_KEEP_COUNT);
    /**
     * 响应线程池
     */
    public static final TaskExecutor RESPONSE_EXECUTOR = TaskExecutor.create("response", MAX_RESPONSE_CONCURRENT_COUNT);

    @Override
    public void run(QuickContext serverContext) throws Exception {
        ConnectionMonitor.daemonRun();
        ServerSocket serverSocket = new ServerSocket(serverContext.port());
        while (super.shouldRunning()) {
            Socket sock = serverSocket.accept();
            LOG.debug("New connection come in.");
            SocketAutonomy.activeAsync(sock);
        }
        serverSocket.close();
    }

    @Override
    protected void onShutdown(QuickContext serverContext) {
        ServerContext.tryGet().executor().shutdown();
        DefaultQuickServer.SOCKET_AUTONOMY_EXECUTOR.shutdown();
        DefaultQuickServer.RESPONSE_EXECUTOR.shutdown();
    }

    /**
     * HTTP/1.1 请求长连接定时清理
     *
     * @author ujued
     */
    static class ConnectionMonitor extends Thread {
        public static final int SOCKET_MAX_FREE_TIME = 1000 * 60;
        private static ConnectionMonitor CONNECTION_MONITOR;
        private BlockingQueue<SocketAutonomy> keepConnections;


        private ConnectionMonitor() {
            keepConnections = new ArrayBlockingQueue<>(MAX_SOCKET_KEEP_COUNT);
        }

        public static void daemonRun() {
            CONNECTION_MONITOR = new ConnectionMonitor();
            CONNECTION_MONITOR.setPriority(Thread.MIN_PRIORITY);
            CONNECTION_MONITOR.setDaemon(true);
            CONNECTION_MONITOR.setName("monitor");
            CONNECTION_MONITOR.start();
        }

        public static void put(SocketAutonomy socketAutonomy) {
            if (Objects.isNull(CONNECTION_MONITOR)) {
                LOG.warn("Put but connectionQueue is null !");
                return;
            }
            CONNECTION_MONITOR.offer(socketAutonomy);
        }

        /**
         * Socket添加到超时检测队列
         *
         * @param socketAutonomy
         */
        public void offer(SocketAutonomy socketAutonomy){
            this.keepConnections.offer(socketAutonomy);
        }

        @Override
        public void run() {
            try {
                while (!this.isInterrupted()) {
                    SocketAutonomy socketAutonomy = this.keepConnections.take();
                    if (socketAutonomy.freeTime() > SOCKET_MAX_FREE_TIME) {
                        // 连接超过一定时间，关闭长连接
                        socketAutonomy.interrupt();
                        socketAutonomy.close();
                        LOG.debug("{} is timeout.", socketAutonomy);
                    } else {
                        // 未超时，重新加入检测队列
                        this.keepConnections.offer(socketAutonomy);
                    }
                    // 检查频率
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
