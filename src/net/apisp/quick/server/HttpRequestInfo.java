/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.apisp.quick.http.HttpRequest;

public class HttpRequestInfo implements HttpRequest {
    private String method;
    private String uri;
    private String version;
    private Map<String, String> headers = new HashMap<>();
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
        if (len == 0) {
            return getWord(buffer);
        }
        byte[] word = new byte[token.flip().limit()];
        token.get(word);
        return new String(word);
    }

    /**
     * 按换行符获取一行Buffer
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
            headers.put(getWord(reqLineBuffer), getWord(reqLineBuffer));
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
}
