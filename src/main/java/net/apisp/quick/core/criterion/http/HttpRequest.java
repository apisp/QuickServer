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
package net.apisp.quick.core.criterion.http;

import net.apisp.quick.old.server.std.BodyBinary;
import net.apisp.quick.std.http.StandardHttpCookie;

/**
 * HTTP请求
 *
 * @author Ujued
 * @date 2018-6-8 11:37:45
 */
public interface HttpRequest {
    
    /**
     * HTTP请求方法
     * 
     * @return GET|POST|DELETE|PUT|HEAD
     */
    String method();

    /**
     * 请求的URI
     * 
     * @return URI
     */
    String uri();

    /**
     * URL?后的请求参数
     * 
     * @return 参数串
     */
    String params();

    /**
     * HTTP版本号
     * 
     * @return 版本号
     */
    String version();

    /**
     * 获取某项请求头
     * 
     * @param key
     * @return
     */
    String header(String key);

    /**
     * 获取某个Cookie
     * 
     * @param key Cookie名
     * @return
     */
    StandardHttpCookie cookie(String key);

    /**
     * 获取所有Cookie
     * 
     * @return
     */
    StandardHttpCookie[] cookies();

    /**
     * 请求体二进制数据
     * 
     * @return
     */
    BodyBinary body();

    /**
     * 请求是否正常
     * 
     * @return
     */
    boolean normative();
    
    /**
     * 请求者IP
     * 
     * @return
     */
    String ip();

    /**
     * 路径变量值
     * 
     * @param variable
     * @return
     */
    Object variable(String variable);
    
    /**
     * 路径变量值
     * 
     * @param variable
     * @param type
     * @return
     */
    <T> T variable(String variable, Class<T> type);
    
}
