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

import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author UJUED
 * @date 2018-06-15 11:35:15
 */
public class SingletonRegister {
    private static final Log LOG = LogFactory.getLog(SingletonRegister.class);
    private Class<?>[] classes;
    private Container container;

    private SingletonRegister(Class<?>[] singletons, Container container) {
        this.classes = singletons;
        this.container = container;
    }

    public static final SingletonRegister prepare(Class<?>[] singletons, Container container) {
        return new SingletonRegister(singletons, container);
    }

    public void register() {
        List<Class<?>> classList = new ArrayList<>();
        for (int i = 0; i < classes.length; i++) {
            classList.add(classes[i]);
        }
        for (int i = 0; i < classList.size(); i++) {
            if (classList.size() > 10000) {
                LOG.warn("构建缓存对象过程中出现了循环引用！终止缓存器！");
                break;
            }
            if (container.singleton(classList.get(i)) != null) {
                continue; // 已有该类单例，跳过，继续下一个
            }
            if (Injections.suitableFor(classList.get(i), container)) {
                try {
                    Object obj = Injections.inject(classList.get(i).newInstance(), container);
                    Class<?>[] ifces = classList.get(i).getInterfaces();
                    if (ifces.length == 0) {
                        container.accept(obj);
                    } else {
                        for (Class<?> ifce : ifces) {
                            if (ObjectLifecycle.class.isAssignableFrom(ifce)) {
                                ObjectLifecycle life = (ObjectLifecycle) obj;
                                life.created();
                                continue;
                            }
                            container.accept(ifce.getName(), obj);
                        }
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    LOG.warn("Class {} is not suitable to cache, losed.", classList.get(i).getName());
                }
            } else { // 稍后缓存
                classList.add(classList.get(i));
            }
        }
    }
}
