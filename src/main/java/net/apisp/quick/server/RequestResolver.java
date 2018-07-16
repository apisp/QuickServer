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
package net.apisp.quick.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.annotation.Nullable;
import net.apisp.quick.core.BodyBinary;
import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.data.DataPersist;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.server.var.FileRequestBody;
import net.apisp.quick.server.var.MemRequestBody;

/**
 * @author UJUED
 * @date 2018-06-12 17:57:57
 */
public class RequestResolver {
    private static final Log LOG = LogFactory.getLog(HttpRequestInfo.class);

    public static final HttpRequestInfo resolve(ByteBuffer requestBuffer) {
        HttpRequestInfo info = new HttpRequestInfo(requestBuffer);
        info.calc();
        return info;
    }

    public static class HttpRequestInfo implements HttpRequest {
        private String method;
        private String uri;
        private String params;
        private String version;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private byte[] body;
        private int bodyOffset;
        private boolean normative = true;
        private InetAddress address;
        private RequestExecutorInfo executorInfo;

        @Nullable
        private DataPersist reqData;

        private BodyBinary bodyBinary;

        private ByteBuffer requestBuffer;

        private HttpRequestInfo(ByteBuffer requestBuffer) {
            this.requestBuffer = requestBuffer;
        }

        public DataPersist getReqData() {
            return reqData;
        }

        public HttpRequestInfo setReqData(DataPersist reqData) {
            this.reqData = reqData;
            return this;
        }

        public HttpRequestInfo setInetSocketAddress(InetAddress address) {
            this.address = address;
            return this;
        }

        public int getBodyOffset() {
            return bodyOffset;
        }

        /**
         * 根据某个字符分词
         * 
         * @param buffer
         * @return
         */
        private String getWord(ByteBuffer buffer, byte character) {
            ByteBuffer token = ByteBuffer.allocate(1024 * 8);
            byte b = 0;
            int len = 0;
            try {
                while (buffer.hasRemaining() && (b = buffer.get()) != character) {
                    token.put(b);
                    len++;
                }
            } catch (Exception e) {
                normative = false; // 请求头异常
            }
            if (len == 0 && buffer.hasRemaining()) {
                return getWord(buffer, character);
            }
            byte[] word = new byte[token.flip().limit()];
            token.get(word);
            return new String(word);
        }

        /**
         * 按换行符获取一行Buffer
         * 
         * @param buffer
         * @return
         */
        private ByteBuffer lineBuffer(ByteBuffer buffer) {
            ByteBuffer token = ByteBuffer.allocate(1024 * 8 * 2);
            byte b = 0;
            try {
                while (buffer.hasRemaining() && (b = buffer.get()) != 10) {
                    this.bodyOffset++;
                    if (b == 13) {
                        continue;
                    }
                    token.put(b);
                }
                this.bodyOffset++;
            } catch (Exception e) {
                normative = false; // 请求头异常
                this.bodyOffset = -1;
            }
            token.flip();
            return token;
        }

        /**
         * 解析出请求行、请求头、请求体
         */
        private void calc() {
            // 请求行
            ByteBuffer reqLineBuffer = lineBuffer(requestBuffer);
            this.method = getWord(reqLineBuffer, (byte) 32);
            this.uri = getWord(reqLineBuffer, (byte) 32);
            int index = -1;
            if ((index = this.uri.indexOf('?')) != -1) {
                this.params = this.uri.substring(index + 1);
                this.uri = this.uri.substring(0, index);
            }
            this.version = getWord(reqLineBuffer, (byte) 32);

            // 请求头
            while ((reqLineBuffer = lineBuffer(requestBuffer)).hasRemaining()) {
                headers.put(getWord(reqLineBuffer, (byte) 58).trim().toUpperCase(),
                        getWord(reqLineBuffer, (byte) 58).trim());
            }

            // cookies
            String ckes = header("Cookie");
            if (ckes != null) {
                String[] cks = ckes.split(";");
                for (int i = 0; i < cks.length; i++) {
                    String[] kv = cks[i].split("=");
                    if (kv.length != 2) {
                        LOG.warn("A cookie losed.");
                        continue;
                    }
                    cookies.put(kv[0].trim().toUpperCase(), kv[1].trim());
                }
            }

            // 请求体
            String bodyLength = header("Content-Length");
            if (bodyLength == null) {
                this.body = new byte[0];
            } else if (Long.valueOf(bodyLength) <= 1024 * 10 - requestBuffer.position()) {
                byte[] theBody = new byte[requestBuffer.limit() - requestBuffer.position()];
                requestBuffer.get(theBody);
                this.body = theBody;
            } else {
                this.body = null;
            }
            this.requestBuffer.clear();

            // BodyBinary
            if (body == null) {
                bodyBinary = new FileRequestBody(this.getReqData(), this.getBodyOffset());
            } else {
                bodyBinary = new MemRequestBody(body);
            }
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public String uri() {
            return uri;
        }

        @Override
        public String header(String key) {
            return headers.get(key.toUpperCase());
        }

        @Override
        public BodyBinary body() {
            return bodyBinary;
        }

        @Override
        public String version() {
            return version;
        }

        @Override
        public boolean normative() {
            return normative;
        }

        @Override
        public HttpCookie cookie(String key) {
            String value = cookies.get(key.toUpperCase());
            if (value == null) {
                return null;
            }
            return new HttpCookie(key, value);
        }

        @Override
        public HttpCookie[] cookies() {
            HttpCookie[] httpCookies = new HttpCookie[cookies.size()];
            Iterator<Map.Entry<String, String>> cookieIterator = cookies.entrySet().iterator();
            int i = 0;
            Map.Entry<String, String> entry = null;
            while (cookieIterator.hasNext()) {
                entry = cookieIterator.next();
                httpCookies[i++] = new HttpCookie(entry.getKey(), entry.getValue());
            }
            return httpCookies;
        }

        @Override
        public String ip() {
            return address.getHostAddress();
        }

        @Override
        public String params() {
            return this.params;
        }

        @Override
        public Object variable(String variable) {
            return executorInfo.getPathVariable(variable, Object.class);
        }

        public void setExecutorInfo(RequestExecutorInfo executorInfo) {
            this.executorInfo = executorInfo;
        }

		@SuppressWarnings("unchecked")
		@Override
		public <T> T variable(String variable, Class<T> type) {
			return (T) executorInfo.getPathVariable(variable, type);
		}
    }
}
