/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server.low;

import java.io.UnsupportedEncodingException;
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

    private String requestInfo;

    private HttpRequestInfo(String requestInfo) {
        this.requestInfo = requestInfo;
    }

    public static HttpRequestInfo create(String requestInfo) {
        HttpRequestInfo info = new HttpRequestInfo(requestInfo);
        info.calc();
        return info;
    }

    private void calc() {
        String separator = "\n\n";
        String headerSeparator = "\n";
        if (requestInfo.contains("\r\n")) {
            separator = "\r\n\r\n";
            headerSeparator = "\r\n";
        }
        String[] headerAndBodyInfo = requestInfo.split(separator);

        // 处理请求行
        String[] headerInfos = headerAndBodyInfo[0].split(headerSeparator);
        String[] methodAndUriAndVersion = headerInfos[0].split(" ");
        this.method = methodAndUriAndVersion[0].trim();
        this.uri = methodAndUriAndVersion[1].trim();
        this.version = methodAndUriAndVersion[2].trim();
        
        // 处理头信息
        for (int i = 1; i < headerInfos.length; i++) {
            String[] kv = headerInfos[i].split(":");
            if (kv.length == 1) { // 头某项不标准, 抛弃
                normative = false;
                continue;
            }
            headers.put(kv[0].trim().toUpperCase(), kv[1].trim());
        }
        int posi = -1;
        if ((posi = this.uri.indexOf('?')) != -1) {
            this.uri = this.uri.substring(0, posi);
        }

        // 处理主体信息
        try {
            this.body = headerAndBodyInfo[1].getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
        }
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
