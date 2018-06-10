/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import java.util.concurrent.ExecutorService;

/**
 * WEB上下文
 * 
 * @author UJUED
 * @date 2018-06-10 11:10:22
 */
public interface WebContext {
    Object setting(String key);

    <T> T singleton(Class<T> type);

    ExecutorService exeutor();
}
