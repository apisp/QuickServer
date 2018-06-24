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

import net.apisp.quick.core.BodyBinary;

/**
 * HTTP请求
 *
 * @author UJUED
 * @date 2018-6-8 11:37:45
 */
public interface HttpRequest {
    String method();

    String uri();

    String version();

    String header(String key);

    HttpCookie cookie(String key);

    HttpCookie[] cookies();

    BodyBinary body();

    boolean normative();
    
    String ip();
}
