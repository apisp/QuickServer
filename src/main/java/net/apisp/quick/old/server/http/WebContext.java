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
package net.apisp.quick.old.server.http;

import net.apisp.quick.ioc.Container;
import net.apisp.quick.thread.TaskExecutor;

/**
 * WEB上下文
 * 
 * @author UJUED
 * @date 2018-06-10 11:10:22
 */
public interface WebContext extends Container {
    
    /**
     * 线程池。“现场能访问到的参数”可以随着任务一起提交
     * 
     * @return 线程池
     */
    TaskExecutor executor();

    /**
     * 编码集
     * 
     * @return 编码集
     */
    String charset();
}
