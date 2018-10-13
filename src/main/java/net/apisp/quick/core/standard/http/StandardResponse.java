package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpCookie;
import net.apisp.quick.core.criterion.http.HttpResponse;
import net.apisp.quick.core.exception.StopServerException;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class StandardResponse implements HttpResponse {
    private Map<String, String> headers = new HashMap<>();
    private List<String> cookies = new ArrayList<>();
    private byte[] body;

    private String charset;

    StandardResponse(String charset) {
        this.charset = charset;
    }

    @Override
    public void header(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public void cookie(HttpCookie cookie) {
        cookies.add(cookie.toString());
    }

    @Override
    public void cookie(String key, String content) {
        HttpCookie httpCookie = new StandardHttpCookie(key, content);
        cookies.add(httpCookie.toString());
    }

    @Override
    public void body(byte[] body) {
        this.body = body;
        headers.put("Content-Length", body.length + "");
    }

    byte[] bytes() throws StopServerException {
        StringBuilder headerStr = new StringBuilder();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            headerStr.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        for (String cookie : cookies) {
            headerStr.append("Set-Cookie").append(cookie).append("\r\n");
        }
        headerStr.append("\r\n");
        try {
            byte[] headerBytes = headerStr.toString().getBytes(charset);
            int responseLength = headerBytes.length + body.length;
            byte[] responseBytes = Arrays.copyOf(headerBytes, responseLength);
            for (int i = headerBytes.length; i < responseLength; i++) {
                responseBytes[i] = body[i];
            }
            return responseBytes;
        } catch (UnsupportedEncodingException e) {
            throw new StopServerException("不支持的编码");
        }
    }
}
