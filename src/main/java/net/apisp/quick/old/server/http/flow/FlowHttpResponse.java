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
package net.apisp.quick.old.server.http.flow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.old.server.ServerContext;

/**
 * 流式响应对象的HTTP实现
 * 
 * @author ujued
 */
public class FlowHttpResponse implements FlowResponse {
    public static final Log LOG = LogFactory.getLog(FlowHttpResponse.class);

    private SocketAndOutputStream sAndO;

    public FlowHttpResponse() {
        this(SocketAndOutputStream.current());
    }

    private FlowHttpResponse(SocketAndOutputStream sAndO) {
        sAndO.setStream(true);
        this.sAndO = sAndO;
        ByteBuffer responseData = ByteBuffer.allocate(1024 * 100);
        responseData.put(String.format("HTTP/1.1 %d %s", 200, "OK").getBytes());
        responseData.put("\r\n".getBytes());
        responseData.put(("Server: QuickServer/1.0").getBytes());
        responseData.put("\r\n".getBytes());
        responseData.put("Content-Type: application/stream+json".getBytes());
        responseData.put("\r\n\r\n".getBytes());
        responseData.flip();
        byte[] data = new byte[responseData.limit()];

        responseData.get(data);
        try {
            sAndO.getOutputStream().write(data);
            sAndO.getOutputStream().flush();
        } catch (IOException e) {
        }
    }

    @Override
    public FlowResponse append(byte[] content) {
        try {
            sAndO.getOutputStream().write(content);
            sAndO.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void over() {
        try {
            sAndO.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FlowResponse append(String content) {
        try {
            return append(content.getBytes(ServerContext.tryGet().charset()));
        } catch (UnsupportedEncodingException e) {
            return append(content.getBytes());
        }
    }

}
