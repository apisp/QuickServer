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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author UJUED
 * @date 2018-06-11 08:36:30
 */
public class DefaultQuickServer extends QuickServer {

    @Override
    public void run(ServerContext serverContext) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open().bind(new InetSocketAddress(serverContext.port()));
        ssc.configureBlocking(false).register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = sChannel.accept();
                    sc.configureBlocking(false).register(selector, SelectionKey.OP_READ);
                    key.interestOps(SelectionKey.OP_ACCEPT);
                } else if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    try {
                        ByteBuffer reqBuffer = ByteBuffer.allocate(1024 * 1024);
                        sc.read(reqBuffer);
                        reqBuffer.flip();
                        HttpRequestInfo reqInfo = HttpRequestInfo.create(reqBuffer);
                        HttpResponseExecutor.prepare(reqInfo, serverContext).execute(sc);
                        key.interestOps(SelectionKey.OP_READ);
                    } catch (IOException e) {
                        LOGGER.debug(e);
                        key.cancel();
                        if (key.channel() != null) {
                            sc.close();
                        }
                    }
                }
            }
        }
    }

}
