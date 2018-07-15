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
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.thread.Task;
import net.apisp.quick.thread.TaskUnit;
import net.apisp.quick.util.Reflects;

/**
 * QuickServer规范
 * 
 * @author UJUED
 * @date 2018-06-08 10:33:31
 */
public abstract class QuickServer {
    private static final Log LOG = LogFactory.getLog(QuickServer.class);
    protected List<TaskUnit> events = new ArrayList<>();
    private ServerContext serverContext;
    private volatile boolean shouldRunning = true;
    
    /**
     * 启动QucikServer
     * 
     * @see Quick
     */
    public final void start() {
        // 前置事件处理
        for (int i = 0; i < events.size(); i++) {
            TaskUnit unit = events.get(i);
            unit.getTask().run(unit.getArgs());
        }
        // 启动QuickServer
        QuickServerThread.boot(serverContext, this);
        // JVM结束钩子
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
        	try {
				this.onShutdown(serverContext);
			} catch (Exception e) {
				LOG.warn("关闭应用时清理逻辑出现了问题！");
			}
        }));
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
	 * 设置Server运行上下文
	 * 
	 * @param serverContext
	 */
	public final void setContext(ServerContext serverContext) {
		this.serverContext = serverContext;
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
	 * @param serverContext
	 * @throws Exception
	 */
	public abstract void run(ServerContext serverContext) throws Exception;

	/**
	 * Server运行逻辑执行完毕后，执行这里，子类的可选实现
	 * 
	 * @param serverContext
	 * @throws Exception
	 */
	protected void onRunning(ServerContext serverContext) throws Exception {
	}

	/**
	 * QuickServer应用关闭时的钩子函数，子类的可选实现
	 * 
	 * @param serverContext
	 * @throws Exception
	 */
	protected void onShutdown(ServerContext serverContext) throws Exception {
	}
}

/**
 * QuickServer主线程
 * 
 * @author ujued
 */
class QuickServerThread extends Thread {
	private static final Log LOG = LogFactory.getLog(QuickServer.class);
	private ServerContext serverContext;
	private QuickServerRunner runner;

	private QuickServerThread(ServerContext serverContext, QuickServerRunner runner) {
		this.serverContext = serverContext;
		this.runner = runner;
		this.setName("server");
	}

	public static void boot(ServerContext serverContext, QuickServer quickServer) {
		new QuickServerThread(serverContext, (context) -> {
			quickServer.run(context);
		}).startAndDoAfterRunning((context) -> {
			quickServer.onRunning(context);
		});
	}

	@Override
	public void run() {
		try {
			runner.run(serverContext);
		} catch (BindException e) {
			Reflects.invoke(serverContext, "setNormative", false);
			LOG.error("The port {} already inuse.", serverContext.port());
		} catch (IOException e) {
			Reflects.invoke(serverContext, "setNormative", false);
			LOG.error("Server start error, IO Exception occered.");
		} catch (Exception e) {
			Reflects.invoke(serverContext, "setNormative", false);
			LOG.error("Server start error, Unkonwn Exception occered. {}", e);
			e.printStackTrace();
		}
	}

	public void startAndDoAfterRunning(QuickServerRunner runner) {
		this.start();
		try {
			Thread.sleep(100);
			if (serverContext.isNormative()) {
				LOG.show("Started Quick Server on port ({})", serverContext.port());
				runner.run(serverContext);
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			LOG.error("Server start success, but after Exception occered.");
		}
	}
}

/**
 * QuickServer运行体引用接口
 * 
 * @author ujued
 */
@FunctionalInterface
interface QuickServerRunner {
	void run(ServerContext serverContext) throws Exception;
}
