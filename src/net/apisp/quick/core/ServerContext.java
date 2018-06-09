/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.apisp.quick.config.Configuration;

/**
 * Server上下文
 *
 * @author UJUED
 * 2018年6月8日 上午9:05:07
 */
public class ServerContext {
    private static ServerContext instance;
    private Map<String, RequestExecutorInfo> mappings;
    private ExecutorService executor;

    private ServerContext() {
        // 单例对象
        mappings = new HashMap<>();
        executor = Executors.newFixedThreadPool((int) Configuration.get("quick.server.threads"));
    }

    static synchronized ServerContext instance() {
        if (instance == null) {
            instance = new ServerContext();
        }
        return instance;
    }

    public void mapping(String key, RequestExecutorInfo executeInfo) {
        mappings.put(key, executeInfo);
    }

    public RequestExecutorInfo hit(String method, String uri) {
        return mappings.get(method.toUpperCase() + " " + uri);
    }

    public ExecutorService executor() {
        return executor;
    }
}
