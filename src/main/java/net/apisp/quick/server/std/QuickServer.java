/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.server.std;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.apisp.quick.core.Quick;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.ServerContext;
import net.apisp.quick.thread.Task;
import net.apisp.quick.thread.TaskUnit;
import net.apisp.quick.util.Reflects;

/**
 * 抽象的QuickServer
 *
 * @author ujued
 */
public abstract class QuickServer {
    private static final Log LOG = LogFactory.getLog(QuickServer.class);
    private List<TaskUnit> events = new ArrayList<>();
    private volatile boolean shouldRunning = true;

    /**
     * 启动QucikServer
     *
     * @see Quick
     */
    public final void start() throws InterruptedException {
        // 前置事件处理
        for (int i = 0; i < events.size(); i++) {
            TaskUnit unit = events.get(i);
            unit.getTask().run(unit.getArgs());
        }
        // 异步启动QuickServer
        QuickContext quickContext = ServerContext.tryGet();
        new Thread(() -> {
            try {
                run(quickContext);
            } catch (BindException e) {
                Reflects.invoke(quickContext, "setNormative", false);
                LOG.error("The port {} already inuse.", quickContext.port());
            } catch (IOException e) {
                Reflects.invoke(quickContext, "setNormative", false);
                LOG.error("Server start error, IO Exception occurred.");
            } catch (Exception e) {
                Reflects.invoke(quickContext, "setNormative", false);
                LOG.error("Server start error, Unknown Exception occurred. {}", e);
                LOG.error(e);
            }
        }).start();
        // 等待QuickServer的实现启动，超时时间是2秒
        synchronized (quickContext) {
            quickContext.wait(2000);
        }
        if (quickContext.isNormative()) {
            LOG.show("Started Quick Server on port {}.", quickContext.port());
        }
        // JVM结束钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.onShutdown(quickContext);
            } catch (Exception e) {
                LOG.warn("关闭应用时清理逻辑出现了问题！");
            }
        }));
    }

    /**
     * Server已启动通知
     */
    public void notifyQuickServerStarted() {
        synchronized (ServerContext.tryGet()) {
            ServerContext.tryGet().notify();
        }
    }

    /**
     * 是否需要运行，一般总是返回true
     *
     * @return
     */
    public final boolean shouldRunning() {
        return this.shouldRunning;
    }

    /**
     * 停止QuickServer
     */
    public final void stop() {
        this.shouldRunning = false;
    }

    /**
     * 添加QuickServer具体运行逻辑执行前的事件
     *
     * @param event
     * @param args
     */
    public final void addEvent(Task event, Object... args) {
        events.add(new TaskUnit(event, args));
    }

    /**
     * 具体的Server运行逻辑
     *
     * @param quickContext
     * @throws Exception
     */
    public abstract void run(QuickContext quickContext) throws Exception;

    /**
     * QuickServer应用关闭时的钩子函数，子类的可选实现
     *
     * @param quickContext
     */
    protected void onShutdown(QuickContext quickContext) {
    }
}