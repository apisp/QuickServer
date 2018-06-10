/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.util.Safes;

/**
 * Server上下文
 *
 * @author UJUED
 * @date 2018-06-08 09:05:07
 */
public class ServerContext {
    private static ServerContext instance;
    private Map<String, RequestExecutorInfo> mappings;
    private ExecutorService executor;
    private boolean normative = true;
    private boolean crossDomain = false;
    
    private Map<String, String> defaultRespHeaders = new HashMap<>();

    private int port;
    private Class<QuickServer> serverClass;

    private ServerContext() {
        // 单例对象
        mappings = new HashMap<>();
        executor = Executors.newFixedThreadPool((int) Configuration.get("server.threads"));

        port = (int) Configuration.get("server.port");
        serverClass = Safes.loadClass(Configuration.get("server").toString(), QuickServer.class);
    }

    /**
     * 第一次调用
     * 
     * @return
     */
    public static synchronized ServerContext instance() {
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

    /**
     * 从配置文件从获取到的QuickServer类
     * 
     * @return
     */
    public Class<QuickServer> getServerClass() {
        return serverClass;
    }

    /**
     * 运行状态
     * 
     * @return
     */
    public boolean isNormative() {
        return normative;
    }

    public void setNormative(boolean normative) {
        this.normative = normative;
    }

    public Object getSetting(String key) {
        return Configuration.get(key);
    }

    public boolean isCrossDomain() {
        return crossDomain;
    }

    public void setCrossDomain(boolean crossDomain) {
        this.crossDomain = crossDomain;
    }

    public Map<String, String> getDefaultRespHeaders() {
        return defaultRespHeaders;
    }
}
