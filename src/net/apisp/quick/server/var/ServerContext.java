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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.apisp.quick.annotation.ReflectionCall;
import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.annotation.ResponseType;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.ioc.Container;
import net.apisp.quick.ioc.SimpleContainer;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.thread.TaskExecutor;
import net.apisp.quick.util.Classpaths;

/**
 * Server上下文
 *
 * @author UJUED
 * @date 2018-06-08 09:05:07
 */
public class ServerContext implements QuickContext {
    private static final Log LOG = LogFactory.getLog(ServerContext.class);
    private static ServerContext instance;
    private Map<String, RequestExecutorInfo> mappings = new HashMap<>();
    private Map<Pattern, RequestExecutorInfo> regMappings = new HashMap<>();
    private int port;
    private TaskExecutor executor;
    private Class<QuickServer> serverClass;
    private boolean normative = true;

    private boolean crossDomain = false;

    private Map<String, String> defaultRespHeaders = new HashMap<>();

    private Container container = new SimpleContainer();

    private ServerContext() {
        port = (int) Configuration.get("server.port");
        serverClass = Classpaths.safeLoadClass(Configuration.get("server").toString(), QuickServer.class);

        executor = TaskExecutor.create((int) Configuration.get("server.threads"));
        defaultRespHeaders.put("Connection", "keep-alive");
    }

    /**
     * 第一次调用
     * 
     * @return
     */
    public static synchronized ServerContext init() {
        if (instance == null) {
            try {
                instance = new ServerContext();
            } catch (Throwable e) {
            }
        }
        return instance;
    }

    /**
     * 尝试获取ServerContext， 再脱离QuickServer调用时，返回null值
     * 
     * @return
     */
    public static ServerContext tryGet() {
        return instance;
    }

    public boolean isCrossDomain() {
        return crossDomain;
    }

    @ReflectionCall("net.apisp.quick.server.QuickServerThread.run()")
    private void setNormative(Boolean normative) {
        this.normative = normative;
    }

    @ReflectionCall("net.apisp.quick.server.MappingResolver.prepare()")
    private void setCrossDomain(Boolean crossDomain) {
        this.crossDomain = crossDomain;
    }

    @Override
    public TaskExecutor executor() {
        return executor;
    }

    @Override
    public String charset() {
        return (String) Configuration.get("charset");
    }

    @Override
    public <T> T singleton(Class<T> type) {
        return container.singleton(type);
    }

    @Override
    public Object singleton(String name) {
        return container.singleton(name);
    }

    @Override
    public Object setting(String key) {
        return Configuration.get(key);
    }

    @Override
    public Path tmpDirPath(String... more) {
        return Paths.get((String) Configuration.get("server.tmp.dir"), more);
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public Class<QuickServer> serverClass() {
        return serverClass;
    }

    @Override
    public Map<String, String> responseHeaders() {
        return defaultRespHeaders;
    }

    @Override
    public RequestExecutorInfo hit(String method, String uri) {
        String key = method.toUpperCase() + " " + uri;
        RequestExecutorInfo info = mappings.get(key);
        if (info != null) {
            return info;
        }
        Iterator<Map.Entry<Pattern, RequestExecutorInfo>> entryIter = regMappings.entrySet().iterator();
        Map.Entry<Pattern, RequestExecutorInfo> entry;
        while (entryIter.hasNext()) {
            entry = entryIter.next();
            if (key.matches(entry.getKey().pattern())) {
                info = entry.getValue();
                Matcher matcher = entry.getKey().matcher(key);
                matcher.find();
                for (int i = 0; i < matcher.groupCount(); i++) {
                    info.addPathVariable(matcher.group(i + 1), i);
                }
                return info;
            }
        }
        return info;
    }

    @Override
    public boolean isNormative() {
        return normative;
    }

    @Override
    public void mapping(String key, RequestExecutorInfo executeInfo) {
        LOG.info("Mapping {} : {}", key, executeInfo.getMethod().toGenericString());
        if (key.indexOf('{') < key.indexOf('}')) {
            String[] md_uri = key.split(" ");
            String httpMethod = md_uri[0];
            String uri = md_uri[1];
            StringBuilder regString = new StringBuilder(httpMethod + "\\s");
            StringBuilder varName = new StringBuilder();
            char[] segment = uri.trim().toCharArray();
            boolean recordStart = false;
            for (int p = 0; p < segment.length; p++) {
                if (segment[p] == '{') {
                    recordStart = true;
                    continue;
                } else if (segment[p] == '}') {
                    recordStart = false;
                    regString.append("([^/]*)?");
                    executeInfo.addPathVariableName(varName.toString());
                    varName.delete(0, varName.length());
                    continue;
                } else if (recordStart) {
                    varName.append(segment[p]);
                    continue;
                } else if (segment[p] == '/') {
                    regString.append("/+");
                    continue;
                } else {
                    regString.append(segment[p]);
                }
            }
            this.regMappings.put(Pattern.compile(regString.toString()), executeInfo);
            return;
        }
        mappings.put(key, executeInfo);
    }

    @Override
    public void mapping(String key, Class<?> controller, String methodName, Class<?>... paramTypes) {
        try {
            Method method = controller.getDeclaredMethod(methodName, paramTypes);
            ResponseType responseType = null;
            if ((responseType = method.getAnnotation(ResponseType.class)) == null) {
                responseType = new ResponseType() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ResponseType.class;
                    }

                    @Override
                    public String value() {
                        return ContentTypes.JSON;
                    }
                };
            }
            RequestExecutorInfo info = new RequestExecutorInfo(method, this.singleton(controller));
            info.addHeader("Content-Type", responseType.value() + "; charset=" + this.charset());
            mapping(key, info);
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.warn(e.getMessage());
        }
    }

    @Override
    public void accept(Object obj) {
        this.container.accept(obj);
    }

    @Override
    public void accept(String name, Object obj) {
        this.container.accept(name, obj);
    }

    @Override
    public Set<String> objects() {
        return this.container.objects();
    }

    @Override
    public <T> T singleton(Class<T> type, boolean safe) {
        return this.container.singleton(type, safe);
    }

    @Override
    public Object singleton(String name, boolean safe) {
        return this.container.singleton(name, safe);
    }

    @Override
    public void accept(String name, ObjectCreaterUnit unit) {
        this.container.accept(name, unit);
    }

    @Override
    public ThreadLocal<?> safeSingleton(String name) {
        return this.container.safeSingleton(name);
    }
}
