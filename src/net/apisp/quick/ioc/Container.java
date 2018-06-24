/**
 * Copyright 2018-present, APISP.NET.
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
package net.apisp.quick.ioc;

import java.util.Set;

import net.apisp.quick.annotation.Nullable;
import net.apisp.quick.server.var.ServerContext;

/**
 * @author UJUED
 * @date 2018-06-15 00:29:29
 */
public interface Container {
    <T> T singleton(Class<T> type);

    Object singleton(String name);

    <T> T singleton(Class<T> type, boolean safe);

    Object singleton(String name, boolean safe);

    ThreadLocal<?> safeSingleton(String name);

    void accept(Object obj);

    void accept(String name, ObjectCreaterUnit creater);

    void accept(String name, Object obj);

    Set<String> objects();

    public static class ObjectCreaterUnit {
        public static interface ObjectCreater {
            Object create(Object... args);
        }

        private ObjectCreaterUnit() {
        }

        public static ObjectCreaterUnit create() {
            return new ObjectCreaterUnit();
        }

        public ObjectCreaterUnit setObjectCreater(ObjectCreater creater) {
            this.creater = creater;
            return this;
        }

        public ObjectCreaterUnit setArgs(Object[] args) {
            this.args = args;
            return this;
        }

        public ObjectCreater getObjectCreater() {
            return this.creater;
        }

        public Object[] getArgs() {
            return this.args;
        }

        private ObjectCreater creater;
        private Object[] args;
    }

    public static class SafeObject<T> {

        @Nullable
        private ThreadLocal<T> threadLocal;
        private String name;

        public SafeObject(String name, ThreadLocal<T> t) {
            this.threadLocal = t;
            this.name = name;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public T get() {
            T r = null;
            if (threadLocal == null) {
                return null;
            }
            if (threadLocal.get() == null) {
                r = (T) ServerContext.tryGet().singleton(name, true);
            } else {
                r = threadLocal.get();
            }
            return r;
        }
    }
}
