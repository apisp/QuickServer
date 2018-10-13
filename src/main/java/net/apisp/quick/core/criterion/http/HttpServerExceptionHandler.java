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

/**
 * 控制器统一异常处理接口
 * 
 * @author Ujued
 * @date 2018-06-26 17:35:20
 */
public interface HttpServerExceptionHandler {
    
    /**
     * 某次请求上下文下异常处理
     * 
     * @param req 请求对象
     * @param resp 响应对象
     * @param e  发生的异常
     */
    void handle(HttpRequest req, HttpResponse resp, Throwable e);
}
