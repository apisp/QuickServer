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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.core.Cacheable;
import net.apisp.quick.ioc.annotation.Autowired;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author UJUED
 * @date 2018-06-15 11:35:15
 */
public class SingletonRegister implements Cacheable<Container> {
    private static final Log LOG = LogFactory.getLog(SingletonRegister.class);
    private List<Class<?>> classList = new ArrayList<>();

    public SingletonRegister() {

    }

    public SingletonRegister set(Class<?>[] classes) {
        this.classList.clear();
        for (int i = 0; i < classes.length; i++) {
            this.classList.add(classes[i]);
        }
        return this;
    }

    @Override
    public Container cache(Container container) {
        boolean canInject = true;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < classList.size(); i++) {
            if (classList.size() > 10000 && System.currentTimeMillis() - startTime < 100) {
                LOG.error("某个单例对象引用了本身");
                break;
            }
            if (container.singleton(classList.get(i)) != null) {
                continue;
            }
            Field[] fields = classList.get(i).getDeclaredFields();
            for (int j = 0; j < fields.length; j++) { // 遍历字段
                if (fields[j].getAnnotation(Autowired.class) != null) {
                    canInject = false;
                    if (container.singleton(fields[j].getType()) != null) {
                        canInject = true;
                    }
                }
            }
            if (canInject) {
                Object obj = null;
                try {
                    obj = classList.get(i).newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < fields.length; j++) { // 遍历字段
                    if (fields[j].getAnnotation(Autowired.class) != null) {
                        fields[j].setAccessible(true);
                        try {
                            fields[j].set(obj, container.singleton(fields[j].getType()));
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                container.accept(obj);
                LOG.debug("Cached object %s", obj);
            } else {
                classList.add(classList.get(i));
            }
            canInject = true;
        }
        return container;
    }
}
