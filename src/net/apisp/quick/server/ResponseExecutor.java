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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestResolver.HttpRequestInfo;
import net.apisp.quick.server.RequestProcessor.ResponseInfo;
import net.apisp.quick.server.var.ServerContext;

public class ResponseExecutor {
    private static final Log LOG = LogFactory.getLog(ResponseExecutor.class);
    private HttpRequestInfo httpRequestInfo;
    private ResponseInfo httpResponseInfo;

    private ResponseExecutor(HttpRequestInfo httpRequestInfo, ResponseInfo httpResponseInfo) {
        this.httpRequestInfo = httpRequestInfo;
        this.httpResponseInfo = httpResponseInfo;
    }

    public static ResponseExecutor execute(HttpRequestInfo httpRequestInfo) {
        ResponseInfo respInfo = RequestProcessor.create(httpRequestInfo).process();
        return new ResponseExecutor(httpRequestInfo, respInfo);
    }

    public void response(OutputStream out) throws IOException {
        ResponseInfo respInfo = this.httpResponseInfo;
        ByteBuffer responseData = ByteBuffer.allocate(1024 * 100);
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
            responseData.put(("Set-Cookie: " + cookies.get(i).toString()).getBytes(ServerContext.tryGet().charset()));
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
        LOG.info("{} {} - {}", httpRequestInfo.method(), httpRequestInfo.uri(), respInfo.getStatus().getCode());
    }

}
