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
package net.apisp.quick.core.http;

/**
 * HTTP响应接口
 * 
 * @author UJUED
 * @date 2018-06-09 23:37:32
 */
public interface HttpResponse {
    void header(String key, String value);

    void cookie(HttpCookie cookie);

    /**
     * 发给客户端一个cookie。path=/ expires=关闭浏览器
     * 
     * @param key
     * @param content
     */
    void cookie(String key, String content);

    void body(byte[] body);
}
