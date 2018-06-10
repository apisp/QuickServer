/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.log.Logger;
import net.apisp.quick.server.RequestProcessor.ResponseInfo;

public class HttpResponseExecutor {
    private static final Logger LOGGER = Logger.get(HttpResponseExecutor.class);
    private HttpRequestInfo httpRequestInfo;
    private ServerContext context;

    private HttpResponseExecutor(HttpRequestInfo requestInfo, ServerContext context) {
        this.httpRequestInfo = requestInfo;
        this.context = context;
    }

    public static HttpResponseExecutor prepare(HttpRequestInfo info, ServerContext context) {
        return new HttpResponseExecutor(info, context);
    }

    public void execute(SocketChannel channel) {
        ResponseInfo respInfo = RequestProcessor
                .create(this.context.hit(httpRequestInfo.method(), httpRequestInfo.uri())).process(httpRequestInfo);
        ByteBuffer responseData = ByteBuffer.allocate(1024 * 1024 * 50);
        // 响应行
        responseData.put(String.format("HTTP/1.1 %d %s", respInfo.getStatus().getCode(), respInfo.getStatus().getDesc())
                .getBytes());
        responseData.put("\r\n".getBytes());

        // 响应头
        Iterator<Map.Entry<String, String>> headerIterator = respInfo.getHeaders().entrySet().iterator();
        Map.Entry<String, String> entry = null;
        while (headerIterator.hasNext()) {
            entry = headerIterator.next();
            responseData.put((entry.getKey() + ": " + entry.getValue()).getBytes());
            responseData.put("\r\n".getBytes());
        }
        responseData.put(("Server: QuickServer/1.0").getBytes());
        responseData.put("\r\n\r\n".getBytes());
        responseData.put(respInfo.getBody());
        responseData.flip();
        while (responseData.hasRemaining()) {
            try {
                channel.write(responseData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("%s %s - %d", httpRequestInfo.method(), httpRequestInfo.uri(), respInfo.getStatus().getCode());
    }

}
