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
package net.apisp.quick.server.var;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.WebContext;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.support.lang.ArgRunnable;

/**
 * @author Ujued
 * @date 2018-06-15 00:53:51
 */
public interface QuickContext extends WebContext {
    
    /**
     * 由请求方式和URI确定后台执行信息
     * 
     * @param method
     * @param uri
     * @return
     */
    RequestExecutorInfo hit(String method, String uri);

    /**
     * 上下文状态
     * @return
     */
    boolean isNormative();

    /**
     * 建立映射关系。URI与执行信息
     * 
     * @param key
     * @param executeInfo
     */
    QuickContext mapping(String key, RequestExecutorInfo executeInfo);
    
    /**
     * 建立映射关系。URI与Function
     * 
     * @param key
     * @param executor
     */
    QuickContext mapping(String key, Function<HttpRequest, Object> executor);
    
    /**
     * 建立映射关系。URI与Supplier
     * 
     * @param key
     * @param executor
     */
    QuickContext mapping(String key, Supplier<Object> executor);

    /**
     * 建立映射关系。URI与Runnable
     * 
     * @param key
     * @param executor
     */
    QuickContext mapping(String key, Runnable executor);
    
    /**
     * 建立映射关系。URI与ArgRunnable
     * 
     * @param key
     * @param executor
     */
    QuickContext mapping(String key, ArgRunnable<HttpRequest> executor);
    
    /**
     * 建立映射关系。URI与执行信息
     * 
     * @param key
     * @param controllerClass
     * @param methodName
     * @param paramTypes
     */
    QuickContext mapping(String key, Class<?> controllerClass, String methodName, Class<?>... paramTypes);

    /**
     * Server监听的端口
     * 
     * @return
     */
    int port();

    /**
     * 全局定义公共响应头
     * 
     * @return
     */
    Map<String, String> responseHeaders();

    /**
     * 确定的QuickServer实现类
     * 
     * @return
     */
    Class<QuickServer> serverClass();

    /**
     * 请求体和响应体等过大时临时文件目录
     * 
     * @param more
     * @return
     */
    Path tmpDirPath(String... more);

}
