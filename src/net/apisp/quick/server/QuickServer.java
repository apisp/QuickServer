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
    private ServerContext serverContext;
    protected List<TaskUnit> events = new ArrayList<>();

    public final void start() {
        TaskUnit unit = null;
        for (int i = 0; i < events.size(); i++) {
            unit = events.get(i);
            unit.getTask().run(unit.getArgs());
        }
        new QuickServerThread(serverContext, (context) -> {
            run(context);
        }).startAndDoAfterRunning((context) -> {
            afterRunning(context);
        });
    }

    public final void setContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public final void addEvent(Task event, Object... args) {
        events.add(new TaskUnit(event, args));
    }

    public abstract void run(ServerContext serverContext) throws Exception;

    protected void afterRunning(ServerContext serverContext) throws Exception {
    }
}

class QuickServerThread extends Thread {
    private static final Log LOG = LogFactory.getLog(QuickServer.class);
    private ServerContext serverContext;
    private QuickServerRunner runner;

    public QuickServerThread(ServerContext serverContext, QuickServerRunner runner) {
        this.serverContext = serverContext;
        this.runner = runner;
        this.setName("server");
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
                LOG.show("Started Quick API Server on port ({})", serverContext.port());
                runner.run(serverContext);
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            LOG.error("Server start success, but after Exception occered.");
        }
    }
}

@FunctionalInterface
interface QuickServerRunner {
    void run(ServerContext serverContext) throws Exception;
}
