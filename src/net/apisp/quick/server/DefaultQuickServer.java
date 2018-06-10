/**
 * Copyright (c) 2018-present, APISP.NET. 
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

    private BlockingQueue<SocketChannel> requestChannels = new ArrayBlockingQueue<>(10);

    @Override
    public void run(ServerContext serverContext) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(serverContext.port()));
        while (true) {
            SocketChannel channel = serverSocketChannel.accept();
            requestChannels.offer(channel);
        }
    }

    @Override
    public void afterRunning(ServerContext serverContext) throws Exception {
        new Thread() {
            @Override
            public void run() {
                ByteBuffer requestData = ByteBuffer.allocate(1024 * 10);
                while (true) {
                    try {
                        SocketChannel socketChannel = requestChannels.take();
                        socketChannel.read(requestData);
                        requestData.flip();
                        HttpRequestInfo info = HttpRequestInfo.create(requestData);
                        HttpResponseExecutor.prepare(info, serverContext).execute(socketChannel);
                        requestData.clear();
                        socketChannel.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
