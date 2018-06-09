/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.apisp.quick.http.ContentTypes;
import net.apisp.quick.http.HttpRequest;
import net.apisp.quick.http.HttpStatus;
import net.apisp.quick.util.JSONs;

/**
 * 请求处理器
 *
 * @author UJUED
 * 2018年6月8日 上午11:11:39
 */
public class RequestProcessor {
    private RequestExecutorInfo executeInfo;

    private RequestProcessor(RequestExecutorInfo info) {
        this.executeInfo = info;
    }

    /**
     * 类工厂
     * 
     * @param info
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
            responseInfo.setStatusCode(HttpStatus.NOT_FOUND);
            responseInfo.setContentType(ContentTypes.HTML);
        } else {
            Method method = executeInfo.getMethod();
            Class<?>[] types = method.getParameterTypes();
            Object[] params = new Object[types.length];
            Class<?> type = null;
            for (int i = 0; i < types.length; i++) {
                type = types[i];
                if (Integer.class.equals(type) || int.class.equals(type)) {
                    params[i] = 0;
                } else if (String.class.equals(type)) {
                    params[i] = new String(request.body());
                } else if (HttpRequest.class.equals(type)) {
                    params[i] = request;
                } else {
                    params[i] = null;
                }
            }
            try {
                Object result = executeInfo.getMethod().invoke(executeInfo.getObject(), params);
                responseInfo.setContentType(executeInfo.getResponseType());
                if (result == null) {
                    responseInfo.setBody(new byte[0]);
                } else {
                    try {
                        String resp = JSONs.convert(result);
                        if (resp == null) {
                            resp = result.toString();
                        }
                        responseInfo.setBody(resp.getBytes("utf8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return responseInfo;
    }

    public static class ResponseInfo {
        private String contentType = ContentTypes.JSON;
        private byte[] body;
        private int statusCode = HttpStatus.OK;

        public ResponseInfo(String contentType, byte[] body) {
            super();
            this.contentType = contentType;
            this.body = body;
        }

        public ResponseInfo() {
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }
}
