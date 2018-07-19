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
package net.apisp.quick.ioc;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

import net.apisp.quick.annotation.Nullable;
import net.apisp.quick.ioc.annotation.Autowired;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.ServerContext;

/**
 * @author UJUED
 * @date 2018-06-15 00:29:29
 */
public interface Container {
    <T> T accept(T obj);

    <T> T accept(String name, T obj);

    void accept(String name, ObjectCreaterUnit creater);

    boolean contains(Class<?> type);

    boolean contains(String name);

    Set<String> objects();

    Object setting(String key);

    <T> T singleton(Class<T> type);

    <T> T singleton(Class<T> type, boolean safe);

    Object singleton(String name);

    Object singleton(String name, boolean safe);

    ThreadLocal<?> safeSingleton(String name);
    
    void unload(String name);
    
    void unload(Class<?> type);

    public static class ObjectCreaterUnit {
        public static interface ObjectCreater {
            Object create(Object... args);
        }

        private ObjectCreater creater;
        private Object[] args;

        private ObjectCreaterUnit(ObjectCreater creater, Object... args) {
            this.creater = creater;
            this.args = args;
        }

        public static ObjectCreaterUnit create(ObjectCreater creater, Object... args) {
            return new ObjectCreaterUnit(creater, args);
        }

        public ObjectCreater getObjectCreater() {
            return this.creater;
        }

        public Object[] getArgs() {
            return this.args;
        }

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

    /**
     * IOC自动注入工具
     * 
     * @author Ujued
     * @date 2018-06-22 11:42:45
     */
    public static abstract class Injections {
        private static final Log LOG = LogFactory.getLog(Injections.class);

        public static boolean suitableFor(Class<?> target, Container container) {
            Field[] fields = target.getDeclaredFields();
            Autowired autowired;
            for (int j = 0; j < fields.length; j++) { // 遍历字段
                if ((autowired = fields[j].getAnnotation(Autowired.class)) != null) {
                    String name = autowired.value();
                    Class<?> safeCls = autowired.safeType();
                    if (name.length() == 0) { // 优先使用注解value值
                        name = safeCls.equals(Void.class) ? fields[j].getName() : safeCls.getName();
                    }
                    if (container.singleton(name) != null || container.safeSingleton(name) != null) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        public static <T> T inject(T obj, Container container) {
            Field[] fields = obj.getClass().getDeclaredFields();
            Autowired autowired = null;
            for (int j = 0; j < fields.length; j++) { // 遍历字段
                if ((autowired = fields[j].getAnnotation(Autowired.class)) != null) {
                    fields[j].setAccessible(true);
                    if (!autowired.safeType().equals(Void.class) && fields[j].getType().equals(SafeObject.class)) {
                        String name = autowired.safeType().getName();
                        SafeObject<?> safe = new SafeObject<>(name, container.safeSingleton(name));
                        try {
                            fields[j].set(obj, safe);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(),
                                    obj.getClass().getName());
                        }
                        continue;
                    }
                    try {
                        String name = autowired.value();
                        if (name.length() == 0) {
                            name = fields[j].getType().getName();
                        }
                        Object v = container.singleton(name);
                        if (Objects.isNull(v)) {
                            if ((v = container.singleton(name, true)) == null) {
                                v = container.setting(name);
                            }
                        }
                        try {
                            fields[j].set(obj, v);
                        } catch (IllegalArgumentException e) {
                            fields[j].set(obj, v.toString());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(), obj.getClass().getName());
                    }
                }
            }
            return obj;
        }
    }
}
