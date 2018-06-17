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
package net.apisp.quick.server.var;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.WebContext;
import net.apisp.quick.ioc.Container;
import net.apisp.quick.ioc.DefaultContainer;
import net.apisp.quick.server.QuickServer;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.thread.TaskExecutor;
import net.apisp.quick.util.Safes;

/**
 * Server上下文
 *
 * @author UJUED
 * @date 2018-06-08 09:05:07
 */
public class ServerContext implements WebContext, QuickContext {
    private static ServerContext instance;
    private Map<String, RequestExecutorInfo> mappings = new HashMap<>();
    private Map<Pattern, RequestExecutorInfo> regMappings = new HashMap<>();
    private int port;
    private TaskExecutor executor;
    private Class<QuickServer> serverClass;
    private boolean normative = true;

    private boolean crossDomain = false;

    private Map<String, String> defaultRespHeaders = new HashMap<>();

    private Container container = new DefaultContainer();

    private ServerContext() {
        port = (int) Configuration.get("server.port");
        serverClass = Safes.loadClass(Configuration.get("server").toString(), QuickServer.class);

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

    public void setNormative(boolean normative) {
        this.normative = normative;
    }

    public boolean isCrossDomain() {
        return crossDomain;
    }

    public void setCrossDomain(boolean crossDomain) {
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
        mappings.put(key, executeInfo);
    }

    @Override
    public void regexMapping(Pattern pattern, RequestExecutorInfo info) {
        this.regMappings.put(pattern, info);
    }

    @Override
    public void accept(Object obj) {
        this.container.accept(obj);
    }

    @Override
    public void accept(String name, Object obj) {
        this.accept(name, obj);
    }
}
