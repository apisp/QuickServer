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
package net.apisp.quick.server;

import java.io.IOException;
import java.io.OutputStream;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestProcessor.ResponseInfo;
import net.apisp.quick.server.RequestResolver.HttpRequestInfo;
import net.apisp.quick.server.flow.FlowException;
import net.apisp.quick.server.flow.FlowResponseExecutor;

/**
 * 响应处理器
 * 
 * @author Ujued
 */
public interface ResponseExecutor {
    static final Log LOG = LogFactory.getLog(ResponseExecutor.class);
    static ResponseExecutor execute(HttpRequestInfo httpRequestInfo, OutputStream out) {
        ResponseInfo respInfo = null;
        try {
            respInfo = RequestProcessor.create(httpRequestInfo).process();
        } catch (FlowException e) {
            LOG.debug("Flow response over.");
            return new FlowResponseExecutor();
        }
        return new HttpResponseExecutor(httpRequestInfo, respInfo, out);
    }
    
	void response() throws IOException;
}
