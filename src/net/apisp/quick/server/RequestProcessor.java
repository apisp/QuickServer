/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.apisp.quick.annotation.RequestBody;
import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.ase.QuickWebContext;
import net.apisp.quick.http.ContentTypes;
import net.apisp.quick.http.HttpRequest;
import net.apisp.quick.http.HttpResponse;
import net.apisp.quick.http.HttpStatus;
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
        if (executeInfo == null) {
            responseInfo.setBody("<h1>Not Found!</h1>".getBytes());
            responseInfo.setStatus(HttpStatus.NOT_FOUND);
            responseInfo.setContentType(ContentTypes.HTML);
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
                    ServerContext context = ServerContext.tryGet();
                    if (context != null) {
                        params[i] = new QuickWebContext(context);
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
                responseInfo.setContentType(executeInfo.getResponseType());
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
        private String contentType;
        private byte[] body = new byte[0];
        private HttpStatus status = HttpStatus.OK;
        private Map<String, String> headers = new HashMap<>();

        public ResponseInfo(String contentType, byte[] body) {
            this.contentType = contentType;
            this.body = body;
        }

        public ResponseInfo normalize() {
            this.headers.put("Content-Length", String.valueOf(body.length));
            this.headers.put("Content-Type", contentType);
            return this;
        }

        void setContentType(String contentType) {
            this.contentType = contentType;
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
            // TODO add cookie
        }

        @Override
        public void body(byte[] body) {
            this.body = body;
        }
    }
}
