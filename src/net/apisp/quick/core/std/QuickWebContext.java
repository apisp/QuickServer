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
package net.apisp.quick.core.std;

import java.util.Set;

import net.apisp.quick.core.http.WebContext;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.thread.TaskExecutor;

public class QuickWebContext implements WebContext {

    private ServerContext serverContext;

    public QuickWebContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public Object setting(String key) {
        return serverContext.setting(key);
    }

    @Override
    public <T> T singleton(Class<T> type) {
        return serverContext.singleton(type);
    }

    @Override
    public TaskExecutor executor() {
        return serverContext.executor();
    }

    @Override
    public String charset() {
        return serverContext.charset();
    }

    @Override
    public Object singleton(String name) {
        return serverContext.singleton(name);
    }

    @Override
    public void accept(Object obj) {
        serverContext.accept(obj);
    }

    @Override
    public void accept(String name, Object obj) {
        serverContext.accept(name, obj);
    }

    @Override
    public Set<String> objects() {
        return serverContext.objects();
    }

    @Override
    public <T> T singleton(Class<T> type, boolean safe) {
        return this.serverContext.singleton(type, safe);
    }

    @Override
    public Object singleton(String name, boolean safe) {
        return this.serverContext.singleton(name, safe);
    }

    @Override
    public void accept(String name, ObjectCreaterUnit creater) {
        this.serverContext.accept(name, creater);
    }

    @Override
    public ThreadLocal<?> safeSingleton(String name) {
        return this.serverContext.safeSingleton(name);
    }

    @Override
    public boolean contains(Class<?> type) {
        return this.serverContext.contains(type);
    }

    @Override
    public boolean contains(String name) {
        return this.serverContext.contains(name);
    }

}
