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
package net.apisp.quick.support.lang;

import java.util.function.Function;

import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.server.var.ServerContext;

/**
 * 快速映射。
 * 在任何可以获取到ServerContext的地方，都可以用serverContext.mapping(key, function)的形式自定义URI映射。
 * 
 * @author ujued
 * @see ServerContext
 */
public class FastRouter {
    public Object route(HttpRequest req, Function<HttpRequest, Object> executor) {
        return executor.apply(req);
    }
    
    public void route(Runnable executor) {
        executor.run();
    }
    
    public void route(HttpRequest req, RunnableWithRequest executor) {
        executor.run(req);
    }
}
