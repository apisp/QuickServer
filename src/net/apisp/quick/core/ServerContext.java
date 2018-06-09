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
 * @date 2018-6-8 9:05:07
 */
public class ServerContext {
    private static ServerContext instance;
    private Map<String, RequestExecutorInfo> mappings;
    private ExecutorService executor;

    private int port;

    private ServerContext() {
        // 单例对象
        mappings = new HashMap<>();
        executor = Executors.newFixedThreadPool((int) Configuration.get("server.threads"));

        port = (int) Configuration.get("server.port");
    }

    static synchronized ServerContext instance() {
        if (instance == null) {
            instance = new ServerContext();
        }
        return instance;
    }

    /**
     * 添加映射
     * 
     * @param key
     * @param executeInfo
     */
    public void mapping(String key, RequestExecutorInfo executeInfo) {
        mappings.put(key, executeInfo);
    }

    /**
     * 命中 mapping
     * 
     * @param method
     * @param uri
     * @return
     */
    public RequestExecutorInfo hit(String method, String uri) {
        return mappings.get(method.toUpperCase() + " " + uri);
    }

    /**
     * 线程池
     * 
     * @return
     */
    public ExecutorService executor() {
        return executor;
    }

    /**
     * 服务监听的端口
     * 
     * @return
     */
    public int port() {
        return port;
    }
}
