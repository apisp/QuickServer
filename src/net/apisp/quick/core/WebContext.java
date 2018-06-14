/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import net.apisp.quick.ioc.Container;
import net.apisp.quick.thread.TaskExecutor;

/**
 * WEB上下文
 * 
 * @author UJUED
 * @date 2018-06-10 11:10:22
 */
public interface WebContext extends Container {
    Object setting(String key);

    TaskExecutor executor();

    String charset();
}
