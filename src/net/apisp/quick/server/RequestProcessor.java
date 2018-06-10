/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.apisp.quick.annotation.RequestBody;
import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.ase.QuickWebContext;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.HttpResponse;
import net.apisp.quick.core.http.HttpStatus;
import net.apisp.quick.util.JSONs;

/**
 * 请求处理器
 *
 * @author UJUED
 * @date 2018-6-8 11:11:39
 */
public class RequestProcessor {
    private RequestExecutorInfo executeInfo;

    private RequestProcessor(RequestExecutorInfo info) {
        this.executeInfo = info;
    }

    /**
     * 创建请求处理器
     * 
     * @param info
     *            处理时需要的信息
     * @return
     */
    public static RequestProcessor create(RequestExecutorInfo info) {
        RequestProcessor processor = new RequestProcessor(info);
        return processor;
    }

    public ResponseInfo process(HttpRequest request) {
        ResponseInfo responseInfo = new ResponseInfo();
        ServerContext serverContext = ServerContext.tryGet();
        if (executeInfo == null) {
            responseInfo.setBody("<h1>Not Found!</h1>".getBytes());
            responseInfo.setStatus(HttpStatus.NOT_FOUND);
            // 注入上下文要求的响应头
            if (serverContext != null) {
                responseInfo.setHeaders(serverContext.getDefaultRespHeaders());
            }
            Map<String, String> contentType = new HashMap<>(1);
            contentType.put("Content-Type", ContentTypes.HTML);
            responseInfo.setHeaders(contentType);
        } else {
            Method method = executeInfo.getMethod();
            Class<?>[] types = method.getParameterTypes();
            Object[] params = new Object[types.length];
            Class<?> type = null;
            Annotation[][] annosParams = method.getParameterAnnotations();
            Annotation[] annos = null;

            // 按类型、注解注入参数
            nextParam: for (int i = 0; i < types.length; i++) {
                type = types[i];

                // 优先按注解注入
                annos = annosParams[i];
                toTypeInject: for (int j = 0; j < annos.length; j++) {
                    if (annos[j] instanceof RequestBody) { // @RequestBody 注入
                        try {
                            if (request.body() == null) {
                                break toTypeInject;
                            }
                            if (type.equals(String.class)) {
                                params[i] = new String(request.body(), "utf8");
                            } else {
                                params[i] = JSONs.convert(new String(request.body(), "utf8"), type);
                            }
                            continue nextParam; // 下一个参数注入
                        } catch (UnsupportedEncodingException e) {
                            // 不会发生
                        }
                    } // else ... 注入
                }

                // 类型
                if (Integer.class.equals(type) || int.class.equals(type)) {
                    params[i] = 0;
                } else if (HttpRequest.class.equals(type)) {
                    params[i] = request;
                } else if (HttpResponse.class.equals(type)) {
                    params[i] = responseInfo;
                } else if (WebContext.class.equals(type)) {
                    if (serverContext != null) {
                        params[i] = new QuickWebContext(serverContext);
                    } else {
                        params[i] = null;
                    }
                } else if (byte[].class.equals(type) || Byte[].class.equals(type)) {
                    params[i] = request.body();
                } else if (String.class.equals(type)) {
                    try {
                        params[i] = new String(request.body(), "utf8");
                    } catch (UnsupportedEncodingException e) {
                    }
                } else {
                    params[i] = null;
                }

            }

            try {
                Object result = executeInfo.getMethod().invoke(executeInfo.getObject(), params);
                serverContext = ServerContext.tryGet();
                // 先注入上下文要求的响应头
                if (serverContext != null) {
                    responseInfo.setHeaders(serverContext.getDefaultRespHeaders());
                }
                // 再注入逻辑方法里做的改动
                responseInfo.setHeaders(executeInfo.getRespHeaders());

                if (method.getReturnType().equals(byte[].class)) {
                    responseInfo.setBody((byte[]) result);
                } else if (result == null) {
                    responseInfo.setBody(responseInfo.body);
                } else {
                    try {
                        String resp = JSONs.convert(result);
                        if (resp == null) {
                            resp = result.toString();
                        }
                        responseInfo.setBody(resp.getBytes("utf8"));
                    } catch (UnsupportedEncodingException e) {
                        // never do here.
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return responseInfo.normalize();
    }

    public static class ResponseInfo implements HttpResponse {
        private byte[] body = new byte[0];
        private HttpStatus status = HttpStatus.OK;
        private Map<String, String> headers = new HashMap<>();
        private List<HttpCookie> cookies = new ArrayList<>();

        public ResponseInfo(byte[] body) {
            this.body = body;
        }

        public ResponseInfo normalize() {
            this.header("Content-Length", String.valueOf(body.length));
            return this;
        }

        void setHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            if (this.headers.get("Content-Type") == null) {
                this.header("Content-Type", ContentTypes.JSON);
            }
        }

        void setBody(byte[] body) {
            this.body = body;
        }

        ResponseInfo() {
        }

        public byte[] getBody() {
            return body;
        }

        public HttpStatus getStatus() {
            return status;
        }

        void setStatus(HttpStatus status) {
            this.status = status;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public void header(String key, String value) {
            headers.put(key, value);
        }

        @Override
        public void cookie(String key, String content) {
            cookies.add(new HttpCookie(key, content));
        }

        @Override
        public void body(byte[] body) {
            this.body = body;
        }

        @Override
        public void cookie(HttpCookie cookie) {
            cookies.add(cookie);
        }

        public List<HttpCookie> getCookies() {
            return cookies;
        }
    }

    /**
     * API触发的事件执行信息
     * 
     * @author UJUED
     * @date 2018-06-08 11:20:09
     */
    public static class RequestExecutorInfo {
        private Method method;
        private Object object;
        private Map<String, String> respHeaders = new HashMap<>();

        public RequestExecutorInfo() {
        }

        public RequestExecutorInfo(Method method, Object object) {
            this.method = method;
            this.object = object;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public RequestExecutorInfo addHeader(String key, String value) {
            this.respHeaders.put(key, value);
            return this;
        }

        public Map<String, String> getRespHeaders() {
            return respHeaders;
        }
    }
}
