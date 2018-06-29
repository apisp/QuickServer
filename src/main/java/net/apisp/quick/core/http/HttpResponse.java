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
package net.apisp.quick.core.http;

/**
 * HTTP响应接口
 * 
 * @author Ujued
 * @date 2018-06-09 23:37:32
 */
public interface HttpResponse {
    
    /**
     * 设置一项响应头
     * 
     * @param key
     * @param value
     */
    void header(String key, String value);

    /**
     * 要求客户端存储一个Cookie
     * 
     * @param cookie
     */
    void cookie(HttpCookie cookie);

    /**
     * 发给客户端一个cookie。path=/ expires=关闭浏览器
     * 
     * @param key
     * @param content
     */
    void cookie(String key, String content);

    /**
     * 设置响应数据
     * 
     * @param body
     */
    void body(byte[] body);
}
