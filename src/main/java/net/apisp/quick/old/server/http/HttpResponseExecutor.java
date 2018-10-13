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
package net.apisp.quick.old.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.apisp.quick.core.criterion.http.HttpCookie;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.old.server.RequestProcessor.ResponseInfo;
import net.apisp.quick.old.server.ServerContext;
import net.apisp.quick.old.server.std.StdHttpRequest;

/**
 * 正常的阻塞响应
 * 
 * @author ujued
 */
public class HttpResponseExecutor implements ResponseExecutor {
	private static final Log LOG = LogFactory.getLog(HttpResponseExecutor.class);
	private StdHttpRequest httpRequestInfo;
	private ResponseInfo httpResponseInfo;
	private OutputStream out;

	public HttpResponseExecutor(StdHttpRequest httpRequestInfo, ResponseInfo httpResponseInfo, OutputStream out) {
		this.httpRequestInfo = httpRequestInfo;
		this.httpResponseInfo = httpResponseInfo;
		this.out = out;
	}

	public void response() throws IOException {
		ResponseInfo respInfo = this.httpResponseInfo;
		responseHeaderData(respInfo, out);
		// 响应体
		out.write(respInfo.getBody());
		out.flush();
		LOG.info("{} {} - {}", httpRequestInfo.method(), httpRequestInfo.uri(), respInfo.getStatus().getCode());
	}

	private void responseHeaderData(ResponseInfo respInfo, OutputStream out) throws IOException {
		ByteBuffer headerData = ByteBuffer.allocate(1024 * 100);
		// 响应行
		headerData.put(String.format("HTTP/1.1 %d %s", respInfo.getStatus().getCode(), respInfo.getStatus().getDesc())
				.getBytes());
		headerData.put("\r\n".getBytes());

		// 响应头
		Iterator<Map.Entry<String, String>> headerIterator = respInfo.getHeaders().entrySet().iterator();
		Map.Entry<String, String> entry = null;
		while (headerIterator.hasNext()) {
			entry = headerIterator.next();
			headerData.put((entry.getKey() + ": " + entry.getValue()).getBytes());
			headerData.put("\r\n".getBytes());
		}
		headerData.put(("Server: QuickServer/1.0").getBytes());
		headerData.put("\r\n".getBytes());
		// Cookies
		List<HttpCookie> cookies = respInfo.getCookies();
		for (int i = 0; i < cookies.size(); i++) {
			headerData.put(("Set-Cookie: " + cookies.get(i).toString()).getBytes(ServerContext.tryGet().charset()));
			headerData.put("\r\n".getBytes());
		}
		headerData.put("\r\n".getBytes());
		headerData.flip();
		byte[] b = new byte[headerData.limit()];
		headerData.get(b);
		out.write(b);
	}
}
