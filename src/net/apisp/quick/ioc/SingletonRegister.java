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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.core.Cacheable;
import net.apisp.quick.ioc.annotation.Accept;
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
    private Class<?>[] factories;
    private Object lock = "";

    public SingletonRegister() {

    }

    public SingletonRegister classes(Class<?>[] classes) {
        this.classList.clear();
        for (int i = 0; i < classes.length; i++) {
            this.classList.add(classes[i]);
        }
        return this;
    }

    @Override
    public Container cache(Container container) {
        Thread byFacs = new Thread() {
            @Override
            public void run() {
                createSingletonsByFactories(container);
            }
        };
        Thread byClss = new Thread() {
            public void run() {
                createSingletonsByClasses(container);
            }
        };
        byFacs.setName("cache-0");
        byClss.setName("cache-1");
        byFacs.start();
        byClss.start();
        try {
            byClss.join();
            byFacs.join();
        } catch (InterruptedException e) {
        }
        return container;
    }

    class AcceptableInfo {
        Method method;
        Object object;
        Class<?>[] paramTypes;
        Accept acceptInfo;

        public AcceptableInfo(Method method, Object object, Class<?>[] paramTypes, Accept acceptInfo) {
            this.method = method;
            this.object = object;
            this.paramTypes = paramTypes;
            this.acceptInfo = acceptInfo;
        }
    }

    private void createSingletonsByFactories(Container container) {
        List<AcceptableInfo> lazyAcceptList = new ArrayList<>();
        Object obj = null;
        Accept acceptInfo = null;
        for (int i = 0; i < factories.length; i++) {
            try {
                obj = factories[i].newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                continue;
            }
            Method[] methods = factories[i].getMethods();

            nextMethod: for (int j = 0; j < methods.length; j++) {
                if ((acceptInfo = methods[j].getAnnotation(Accept.class)) != null) {
                    Class<?>[] paramTypes = methods[j].getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int k = 0; k < paramTypes.length; k++) {
                        params[k] = container.singleton(paramTypes[k]);
                        if (params[k] == null) {
                            lazyAcceptList.add(new AcceptableInfo(methods[j], obj, paramTypes, acceptInfo));
                            break nextMethod;
                        }
                    }
                    try {
                        Object acceptable = methods[j].invoke(obj, params);
                        if (acceptable != null) {
                            if (acceptInfo.value().length() > 0) {
                                container.accept(acceptInfo.value(), acceptable);
                            } else {
                                container.accept(acceptable);
                            }
                            LOG.debug("Cached object %s", acceptable);
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // lazyAccept
        long startTime = System.currentTimeMillis();
        nextAcceptable: for (int i = 0; i < lazyAcceptList.size(); i++) {
            if (lazyAcceptList.size() > 1000 && System.currentTimeMillis()
                    - startTime < (Runtime.getRuntime().availableProcessors() >= 2 ? 10 : 20)) {
                try {
                    lock.wait(200);
                    LOG.debug("Waiting for thread cache-1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (lazyAcceptList.size() > 10000 && System.currentTimeMillis()
                    - startTime < (Runtime.getRuntime().availableProcessors() >= 2 ? 110 : 220)) {
                LOG.error("Factory 构建对象过程中出现了循环引用！");
                break;
            }
            Class<?>[] paramTypes = lazyAcceptList.get(i).paramTypes;
            Object[] params = new Object[paramTypes.length];
            obj = lazyAcceptList.get(i).object;
            acceptInfo = lazyAcceptList.get(i).acceptInfo;

            for (int k = 0; k < paramTypes.length; k++) {
                params[k] = container.singleton(paramTypes[k]);
                if (params[k] == null) {
                    lazyAcceptList.add(lazyAcceptList.get(i));
                    LOG.debug("size:%d, i:%d", lazyAcceptList.size(), i);
                    continue nextAcceptable;
                }
            }
            try {
                Object acceptable = lazyAcceptList.get(i).method.invoke(obj, params);
                if (acceptable != null) {
                    if (acceptInfo.value().length() > 0) {
                        container.accept(acceptInfo.value(), acceptable);
                    } else {
                        container.accept(acceptable);
                    }
                    LOG.debug("Cached object %s", acceptable);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public SingletonRegister factories(Class<?>[] factories) {
        this.factories = factories;
        return this;
    }

    private void createSingletonsByClasses(Container container) {
        boolean canInject = true;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < classList.size(); i++) {
            if (classList.size() > 1000 && System.currentTimeMillis()
                    - startTime < (Runtime.getRuntime().availableProcessors() >= 2 ? 10 : 20)) {
                try {
                    lock.wait(200);
                    LOG.debug("Waiting for thread cache-1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (classList.size() > 10000 && System.currentTimeMillis()
                    - startTime < (Runtime.getRuntime().availableProcessors() >= 2 ? 110 : 220)) {
                LOG.error("构建对象过程中出现了循环引用！");
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
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
