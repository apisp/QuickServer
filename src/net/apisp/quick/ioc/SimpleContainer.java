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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author UJUED
 * @date 2018-06-15 00:29:50
 */
public class SimpleContainer implements Container {
    private static Log LOG = LogFactory.getLog(SimpleContainer.class);
    private Map<String, Object> cache = new HashMap<>();
    private Map<String, ObjectCreaterUnit> safeObjectCreaters = new HashMap<>();
    private Map<String, ThreadLocal<Object>> safeCache = new HashMap<>();

    public SimpleContainer() {
    }

    @Override
    public <T> T singleton(Class<T> type) {
        return singleton(type, false);
    }

    @Override
    public Object singleton(String name) {
        return cache.get(name);
    }

    @Override
    public void accept(Object obj) {
        this.cache.put(obj.getClass().getName(), obj);
        LOG.info("Cached object {}", obj.getClass().getName());
    }

    @Override
    public void accept(String name, Object obj) {
        this.cache.put(name, obj);
        LOG.info("Cached object {}({})", name, obj.getClass().getName());
    }

    @Override
    public Set<String> objects() {
        return this.cache.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T singleton(Class<T> type, boolean safe) {
        return (T) singleton(type.getName(), safe);
    }

    @Override
    public Object singleton(String name, boolean safe) {
        if (safe) {
            ThreadLocal<Object> obj = this.safeCache.get(name);
            if (obj == null) {
                return null;
            }
            if (obj.get() != null) {
                return obj.get();
            } else {
                ObjectCreaterUnit unit = this.safeObjectCreaters.get(name);
                obj.set(unit.getObjectCreater().create(unit.getArgs()));
                return obj.get();
            }
        } else {
            return cache.get(name);
        }
    }

    @Override
    public void accept(String name, ObjectCreaterUnit unit) {
        this.safeObjectCreaters.put(name, unit);
        this.safeCache.put(name, new ThreadLocal<>());
        LOG.info("Safe cached object {}", name);
    }

    @Override
    public ThreadLocal<?> safeSingleton(String name) {
        return this.safeCache.get(name);
    }

    @Override
    public Object setting(String key) {
        return Configuration.get(key);
    }

    @Override
    public boolean contains(Class<?> type) {
        return Objects.nonNull(this.singleton(type));
    }

    @Override
    public boolean contains(String name) {
        return Objects.nonNull(this.singleton(name));
    }
}
