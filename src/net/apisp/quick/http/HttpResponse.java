/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.http;

/**
 * HTTP响应接口
 * 
 * @author UJUED
 * @date 2018-06-09 23:37:32
 */
public interface HttpResponse {
    void header(String key, String value);

    void cookie(String key, String content);

    void body(byte[] body);
}
