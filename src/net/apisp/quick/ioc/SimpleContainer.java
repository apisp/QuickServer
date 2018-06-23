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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author UJUED
 * @date 2018-06-15 00:29:50
 */
public class SimpleContainer implements Container {

    private static Log LOG = LogFactory.getLog(SimpleContainer.class);
    private Map<String, Object> cache = new HashMap<>();

    public SimpleContainer() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T singleton(Class<T> type) {
        return (T) cache.get(type.getName());
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
}
