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
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import net.apisp.quick.annotation.ReflectionCall;
import net.apisp.quick.core.BodyBinary;
import net.apisp.quick.core.annotation.RequestBody;
import net.apisp.quick.core.annotation.Variable;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.core.http.ExceptionHandler;
import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.HttpResponse;
import net.apisp.quick.core.http.HttpStatus;
import net.apisp.quick.core.http.WebContext;
import net.apisp.quick.core.std.QuickWebContext;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestResolver.HttpRequestInfo;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.template.T;
import net.apisp.quick.util.Classpaths;
import net.apisp.quick.util.JSONs;
import net.apisp.quick.util.Strings;

/**
 * 请求处理器。控制器逻辑处理
 *
 * @author UJUED
 * @date 2018-6-8 11:11:39
 */
public class RequestProcessor {
    private static final Log LOG = LogFactory.getLog(RequestProcessor.class);
    private RequestExecutorInfo executeInfo;
    private HttpRequestInfo httpRequest;

    private ServerContext serverContext = ServerContext.tryGet(); {
        if (serverContext == null) {
            throw new IllegalStateException("ServerContext get error.");
        }
    }

    private RequestProcessor(HttpRequestInfo httpRequest) {
        this.httpRequest = httpRequest;
        this.executeInfo = serverContext.hit(httpRequest.method(), httpRequest.uri());
    }

    private void responseInfoApplyStatus(ResponseInfo responseInfo, HttpStatus status) {
        String userAgent = httpRequest.header("User-Agent");
        if (userAgent != null && userAgent.contains("Mozilla")) {
            String sc = String.valueOf(status.getCode());
            Optional<String> body = Optional.ofNullable((String) serverContext.singleton(sc + ".html"));
            responseInfo.body = Strings.bytes(body.orElse(sc), serverContext.charset());
        } else {
            responseInfo.body = (status.getCode() + " " + status.getDesc()).getBytes();
        }
        responseInfo.status = status;
        Map<String, String> contentType = new HashMap<>(1);
        contentType.put("Content-Type", ContentTypes.HTML + ";charset=" + serverContext.charset());
        responseInfo.ensureHeaders(contentType);
    }

    /**
     * 创建请求处理器
     * 
     * @param info
     *            处理时需要的信息
     * @return
     */
    public static RequestProcessor create(HttpRequestInfo req) {
        RequestProcessor processor = new RequestProcessor(req);
        return processor;
    }

    public ResponseInfo process() {
        ResponseInfo responseInfo = new ResponseInfo();
        // 上下文要求的响应头
        responseInfo.ensureHeaders(serverContext.responseHeaders());

        HttpRequestInfo request = this.httpRequest;
        if (!request.normative()) {
            // 400 Bad Request //////////////////////////////////////////////
            responseInfoApplyStatus(responseInfo, HttpStatus.BAD_REQUEST);
            return responseInfo.normalize();
        }
        if (executeInfo == null) {
            responseInfoApplyStatus(responseInfo, HttpStatus.NOT_FOUND);
            return responseInfo.normalize();
        }
        Method method = executeInfo.getMethod();
        Map<String, Object> model = new HashMap<>();
        Class<?>[] types = method.getParameterTypes();
        Object[] params = new Object[types.length];
        Class<?> type = null; // 参数类型
        Annotation[][] annosParams = method.getParameterAnnotations();
        Annotation[] annos = null;

        // 按类型、注解注入参数 //////////////////////////////////////////////
        nextParam: for (int i = 0; i < types.length; i++) {
            type = types[i];

            // 优先按注解注入 ///////////////////////////////////////////////
            annos = annosParams[i];
            toTypeInject: for (int j = 0; j < annos.length; j++) {
                if (annos[j] instanceof RequestBody) {
                    if (request.body() == null) {
                        break toTypeInject;
                    }
                    if (request.body().length() > 1024 * 2) {
                        continue nextParam;
                    }
                    if (JSONObject.class.isAssignableFrom(type)) { // JSONObject类型
                        byte[] bin = request.body().data(0, (int) request.body().length());
                        params[i] = new JSONObject(Strings.toString(bin, serverContext.charset()));
                    }
                    continue nextParam; // 开始下一个参数注入
                } else if (annos[j] instanceof Variable) {
                    Variable pv = (Variable) annos[j];
                    params[i] = executeInfo.getPathVariable(pv.value(), type);
                    continue nextParam;
                }
            }

            // 类型注入 ///////////////////////////////////////////////////
            if (JSONObject.class.isAssignableFrom(type)) {
                byte[] bin = request.body().data(0, (int) request.body().length());
                params[i] = new JSONObject(Strings.toString(bin, serverContext.charset()));
            } else if (HttpRequest.class.isAssignableFrom(type)) {
                params[i] = request;
            } else if (HttpResponse.class.isAssignableFrom(type)) {
                params[i] = responseInfo;
            } else if (WebContext.class.isAssignableFrom(type)) {
                params[i] = new QuickWebContext(serverContext);
            } else if (BodyBinary.class.isAssignableFrom(type)) {
                params[i] = request.body();
            } else if (Map.class.isAssignableFrom(type)) {
                params[i] = model;
            } else if (Integer.class.equals(type) || int.class.equals(type)) {
                params[i] = 0;
            } else {
                params[i] = serverContext.singleton(type);
            }
        }

        try {
            Object controller = executeInfo.getObject();
            Object result = method.invoke(controller, params);

            // 逻辑方法执行完毕，关闭请求文件
            if (request.getReqData() != null) {
                request.getReqData().close();
            }
            // 注入逻辑方法里改动的Headers
            responseInfo.ensureHeaders(executeInfo.getRespHeaders());
            if (result == null) {
                // 已经设置好body
                LOG.debug("{}.{}(..) return type is null.", controller.getClass().getName(), method.getName());
            } else if (JSONObject.class.isAssignableFrom(method.getReturnType())) {
                responseInfo.body = Strings.bytes(result.toString(), serverContext.charset());
            } else if (method.getReturnType().equals(byte[].class)) {
                responseInfo.body = (byte[]) result;
            } else if (executeInfo.getViewDirectory() != null && result instanceof String) {
                T tp = serverContext.singleton(T.class).setVariables(model);
                Path file = Classpaths.get(executeInfo.getViewDirectory() + (String) result);
                if (executeInfo.respHeaders.get("Content-Type").contains("text")) {
                    responseInfo.body = tp.render2bin(file);
                } else {
                    responseInfo.body = Files.readAllBytes(file);
                }

            } else {
                String resp = JSONs.convert(result);
                if (resp == null) {
                    resp = result.toString();
                }
                responseInfo.body = Strings.bytes(resp, serverContext.charset());
            }
        } catch (InvocationTargetException e) {
            // 逻辑异常，这里进行统一异常处理 ////////////////////////////////////////
            ExceptionHandler handler = serverContext.singleton(ExceptionHandler.class);
            handler.handle(httpRequest, responseInfo, e.getCause());
        } catch (IllegalAccessException | IllegalArgumentException e) {
            LOG.debug(e);
        } catch (IOException e) {
            // 404 Not Found
            responseInfoApplyStatus(responseInfo, HttpStatus.NOT_FOUND);
        }
        return responseInfo.normalize();
    }

    /**
     * 响应信息，设置body后，自动在响应头注入Content-Length
     * 
     * @author UJUED
     * @date 2018-06-11 09:48:46
     */
    public static class ResponseInfo implements HttpResponse {
        private byte[] body = new byte[0];
        private HttpStatus status = HttpStatus.OK;
        private Map<String, String> headers = new HashMap<>();
        private List<HttpCookie> cookies = new ArrayList<>();

        public ResponseInfo(byte[] body) {
            this.body = body;
        }

        @ReflectionCall("net.apisp.quick.core.std.QuickExceptionHandler.handle(..)")
        private void setHttpStatus(HttpStatus status) {
            this.status = status;
        }

        /**
         * 使用之前必须调用本方法
         * 
         * @return
         */
        public ResponseInfo normalize() {
            this.header("Content-Length", String.valueOf(body.length));
            return this;
        }

        void ensureHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            if (this.headers.get("Content-Type") == null) {
                String charset = ServerContext.tryGet() == null ? "UTF-8" : ServerContext.tryGet().charset();
                this.header("Content-Type", ContentTypes.JSON + ";charset=" + charset);
            }
        }

        ResponseInfo() {
        }

        public byte[] getBody() {
            return body;
        }

        public HttpStatus getStatus() {
            return status;
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
        private Map<String, Object> pathVariables = new HashMap<>();
        private List<String> pathVariableNames = new ArrayList<>();
        private String viewDirectory;

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

        public String getViewDirectory() {
            return viewDirectory;
        }

        public void setViewDirectory(String viewDirectory) {
            this.viewDirectory = viewDirectory;
        }

        public RequestExecutorInfo addHeader(String key, String value) {
            this.respHeaders.put(key, value);
            return this;
        }

        public Map<String, String> getRespHeaders() {
            return respHeaders;
        }

        public void addPathVariableName(String name) {
            this.pathVariableNames.add(name);
        }

        public Object getPathVariable(String key, Class<?> type) {
            Object obj = pathVariables.get(key);
            String c = object.getClass().getName();
            String m = method.getName();
            if (obj == null) {
                return null;
            }
            if (obj.getClass().equals(type)) { // String
                try {
                    return URLDecoder.decode(obj.toString(), ServerContext.tryGet().charset());
                } catch (UnsupportedEncodingException e) {
                    return obj;
                }
            } else if (Integer.class.equals(type) || int.class.equals(type)) {
                int intVal = -1;
                try {
                    intVal = Integer.valueOf(obj.toString());
                } catch (NumberFormatException e) {
                    LOG.warn("At {}.{}. PathVariable [{}] can't format to int.", c, m, key);
                }
                return intVal;
            } else if (Double.class.equals(type) || double.class.equals(type)) {
                double doubleVal = -1.0;
                try {
                    doubleVal = Double.valueOf(obj.toString());
                } catch (NumberFormatException e) {
                    LOG.warn("At {}.{}. PathVariable [{}] can't format to double.", c, m, key);
                }
                return doubleVal;
            } else if (Long.class.equals(type) || long.class.equals(type)) {
                long longVal = -1;
                try {
                    longVal = Long.valueOf(obj.toString());
                } catch (NumberFormatException e) {
                    LOG.warn("At {}.{}(). PathVariable [{}] can't format to long.", c, m, key);
                }
                return longVal;
            } else if (Date.class.equals(type)) {
                Date d = null;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    d = sdf.parse(obj.toString());
                } catch (ParseException e) {
                    LOG.warn("At {}.{}. PathVariable [{}] can't format to date.", c, m, key);
                }
                return d;
            } else {
                return obj.toString();
            }
        }

        public void addPathVariable(String value, int index) {
            this.pathVariables.put(this.pathVariableNames.get(index), value);
        }
    }
}
