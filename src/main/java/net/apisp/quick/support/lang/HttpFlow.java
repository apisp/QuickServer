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

import net.apisp.quick.server.flow.FlowHttpResponse;
import net.apisp.quick.server.flow.FlowResponse;

/**
 * HTTP流式工具
 * 
 * @author ujued
 */
public abstract class HttpFlow {
    private static final ThreadLocal<FlowResponse> respLocal = new ThreadLocal<>();

    /**
     * 获取当前的响应流
     * 
     * @return
     */
    public static final synchronized FlowResponse response() {
        if (respLocal.get() == null) {
            respLocal.set(new FlowHttpResponse());
        }
        return respLocal.get();
    }
}
