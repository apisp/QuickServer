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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DefaultQuickServer extends QuickServer {

    private BlockingQueue<SocketChannel> sockes = new ArrayBlockingQueue<>(10);

    @Override
    public void run(ServerContext serverContext) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(serverContext.port()));
        while (true) {
            SocketChannel sc = serverSocketChannel.accept();
            sockes.offer(sc);
        }
    }

    @Override
    public void afterRunning(ServerContext serverContext) throws Exception {
        while (true) {
            SocketChannel sc = sockes.take();
            ByteBuffer requestBuffer = ByteBuffer.allocate(1024 * 1024);
            sc.read(requestBuffer);
            requestBuffer.flip();
            HttpRequestInfo requestInfo = HttpRequestInfo.create(requestBuffer);
            HttpResponseExecutor.prepare(requestInfo, serverContext).execute(sc);
        }
    }

}
