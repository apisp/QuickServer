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

import net.apisp.quick.core.http.WebContext;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;

/**
 * @author UJUED
 * @date 2018-06-15 00:53:51
 */
public interface QuickContext extends WebContext {
    RequestExecutorInfo hit(String method, String uri);

    boolean isNormative();

    void mapping(String key, RequestExecutorInfo executeInfo);

    void mapping(String key, Class<?> controllerClass, String methodName, Class<?>... paramTypes);

    int port();

    Map<String, String> responseHeaders();

    Class<QuickServer> serverClass();

    Path tmpDirPath(String... more);

}
