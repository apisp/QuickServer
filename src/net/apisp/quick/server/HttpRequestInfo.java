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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.log.Logger;

public class HttpRequestInfo implements HttpRequest {
    private static final Logger LOGGER = Logger.get(HttpRequestInfo.class);
    private String method;
    private String uri;
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private byte[] body;
    private boolean normative = true;

    private ByteBuffer reqInfo;

    private HttpRequestInfo(ByteBuffer requestInfo) {
        this.reqInfo = requestInfo;
    }

    public static HttpRequestInfo create(ByteBuffer reqInfo) {
        HttpRequestInfo info = new HttpRequestInfo(reqInfo);
        info.calc0();
        return info;
    }

    /**
     * 根据空格和:分词
     * 
     * @param buffer
     * @return
     */
    private String getWord(ByteBuffer buffer) {
        ByteBuffer token = ByteBuffer.allocate(1024);
        byte b = 0;
        int len = 0;
        while (buffer.hasRemaining() && (b = buffer.get()) != 32 && b != 58) {
            token.put(b);
            len++;
        }
        if (len == 0 && buffer.hasRemaining()) {
            return getWord(buffer);
        }
        byte[] word = new byte[token.flip().limit()];
        token.get(word);
        return new String(word);
    }

    /**
     * 按换行符获取一行Buffer
     * 
     * @param buffer
     * @return
     */
    private ByteBuffer lineBuffer(ByteBuffer buffer) {
        ByteBuffer token = ByteBuffer.allocate(1024 * 2);
        byte b = 0;
        while (buffer.hasRemaining() && (b = buffer.get()) != 10) {
            if (b == 13) {
                continue;
            }
            token.put(b);
        }
        token.flip();
        return token;
    }

    /**
     * 解析出请求行、请求头、请求体
     */
    private void calc0() {
        // 请求行
        ByteBuffer reqLineBuffer = lineBuffer(reqInfo);
        this.method = getWord(reqLineBuffer);
        this.uri = getWord(reqLineBuffer);
        this.version = getWord(reqLineBuffer);

        // 请求头
        while ((reqLineBuffer = lineBuffer(reqInfo)).hasRemaining()) {
            headers.put(getWord(reqLineBuffer).toUpperCase(), getWord(reqLineBuffer));
        }

        // cookies
        String ckes = header("Cookie");
        if (ckes != null) {
            String[] cks = ckes.split(";");
            for (int i = 0; i < cks.length; i++) {
                String[] kv = cks[i].split("=");
                if (kv.length != 2) {
                    LOGGER.warn("A cookie losed.");
                    continue;
                }
                cookies.put(kv[0].trim().toUpperCase(), kv[1].trim());
            }
        }

        // 请求体
        byte[] theBody = new byte[reqInfo.limit() - reqInfo.position()];
        reqInfo.get(theBody);
        this.body = theBody;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String header(String key) {
        return headers.get(key.toUpperCase());
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public boolean normative() {
        return normative;
    }

    @Override
    public HttpCookie cookie(String key) {
        return new HttpCookie(key, cookies.get(key.toUpperCase()));
    }

    @Override
    public HttpCookie[] cookies() {
        HttpCookie[] httpCookies = new HttpCookie[cookies.size()];
        Iterator<Map.Entry<String, String>> cookieIterator = cookies.entrySet().iterator();
        int i = 0;
        Map.Entry<String, String> entry = null;
        while (cookieIterator.hasNext()) {
            entry = cookieIterator.next();
            httpCookies[i++] = new HttpCookie(entry.getKey(), entry.getValue());
        }
        return httpCookies;
    }
}