/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.HttpRequestResolver.HttpRequestInfo;
import net.apisp.quick.server.RequestProcessor.ResponseInfo;

public class HttpResponseExecutor {
    private static final Log LOG = LogFactory.getLog(HttpResponseExecutor.class);
    private HttpRequestInfo httpRequestInfo;
    private ResponseInfo httpResponseInfo;

    private HttpResponseExecutor(HttpRequestInfo httpRequestInfo, ResponseInfo httpResponseInfo) {
        this.httpRequestInfo = httpRequestInfo;
        this.httpResponseInfo = httpResponseInfo;
    }

    public static HttpResponseExecutor execute(HttpRequestInfo httpRequestInfo) {
        ResponseInfo respInfo = RequestProcessor.create(httpRequestInfo).process();
        return new HttpResponseExecutor(httpRequestInfo, respInfo);
    }

    public void response(OutputStream out) throws IOException {
        ResponseInfo respInfo = this.httpResponseInfo;
        ByteBuffer responseData = ByteBuffer.allocate(1024 * 50);
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
        responseData.put("\r\n".getBytes());
        // Cookies
        List<HttpCookie> cookies = respInfo.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            responseData.put(("Set-Cookie: " + cookies.get(i).toString()).getBytes("utf8"));
            responseData.put("\r\n".getBytes());
        }
        responseData.put("\r\n".getBytes());

        // 响应体
        responseData.put(respInfo.getBody());
        responseData.flip();

        byte[] rD = new byte[responseData.limit()];
        responseData.get(rD);
        responseData = null;
        out.write(rD);
        LOG.info("%s %s - %d", httpRequestInfo.method(), httpRequestInfo.uri(), respInfo.getStatus().getCode());
    }

}
