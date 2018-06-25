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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.core.Cacheable;
import net.apisp.quick.ioc.Container.Injections;
import net.apisp.quick.ioc.Container.ObjectCreaterUnit;
import net.apisp.quick.ioc.annotation.Accept;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.util.Strings;

/**
 * @author UJUED
 * @date 2018-06-15 11:35:15
 */
public class SingletonRegister implements Cacheable<Container> {
    private static final Log LOG = LogFactory.getLog(SingletonRegister.class);
    private Object lock = new Object();
    private Class<?>[] classes;
    private Class<?>[] factories;

    public SingletonRegister() {

    }

    public SingletonRegister classes(Class<?>[] classes) {
        this.classes = classes;
        return this;
    }

    public SingletonRegister factories(Class<?>[] factories) {
        this.factories = factories;
        return this;
    }

    @Override
    public Container cache(Container container) {
        Thread byFacs = new Thread(() -> createSingletonsByFactories(container));
        Thread byClss = new Thread(() -> createSingletonsByClasses(container));
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

    /**
     * 从指定的Factories中缓存对象
     * 
     * @param container
     */
    private void createSingletonsByFactories(Container container) {
        List<AcceptableInfo> acceptList = new ArrayList<>();
        Object obj = null;
        Accept acceptInfo = null;
        for (int i = 0; i < factories.length; i++) {
            try {
                obj = factories[i].newInstance();
                Injections.inject(obj, container);
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
                            acceptList.add(new AcceptableInfo(methods[j], obj, paramTypes, acceptInfo));
                            continue nextMethod;
                        }
                    }
                    if (acceptInfo.safe()) {
                        String name = methods[j].getReturnType().getName();
                        if (acceptInfo.value().length() > 0) {
                            name = acceptInfo.value();
                        }
                        ObjectCreaterUnit unit = ObjectCreaterUnit.create().setObjectCreater((args) -> {
                            Method m = (Method) args[0];
                            Object exe = args[1];
                            Object[] pars = (Object[]) args[2];
                            try {
                                Object acceptable = m.invoke(exe, pars);
                                return acceptable;
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }).setArgs(new Object[] { methods[j], obj, params });
                        container.accept(name, unit);
                        continue nextMethod;
                    }
                    try {
                        Object acceptable = methods[j].invoke(obj, params);
                        if (acceptable != null) {
                            if (acceptInfo.value().length() > 0) {
                                container.accept(acceptInfo.value(), acceptable);
                            } else {
                                container.accept(methods[j].getReturnType().getName(), acceptable);
                            }
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        LOG.warn("Class {} not cached. Cause: {}", obj.getClass().getName(), e.getMessage());
                    }
                }
            }
        }
        // lazyAccept
        long startTime = System.currentTimeMillis();
        nextAcceptable: for (int i = 0; i < acceptList.size(); i++) {
            try {
                resolveDependence(acceptList.size(), startTime);
            } catch (InterruptedException e) {
                break;
            }
            Class<?>[] paramTypes = acceptList.get(i).paramTypes;
            Object[] params = new Object[paramTypes.length];
            obj = acceptList.get(i).object;
            acceptInfo = acceptList.get(i).acceptInfo;

            for (int k = 0; k < paramTypes.length; k++) {
                params[k] = container.singleton(paramTypes[k]);
                if (params[k] == null) {
                    acceptList.add(acceptList.get(i));
                    continue nextAcceptable;
                }
            }

            if (acceptInfo.safe()) {
                String name = acceptList.get(i).method.getReturnType().getName();
                if (acceptInfo.value().length() > 0) {
                    name = acceptInfo.value();
                }
                ObjectCreaterUnit unit = ObjectCreaterUnit.create().setObjectCreater((args) -> {
                    Method m = (Method) args[0];
                    Object exe = args[1];
                    Object[] pars = (Object[]) args[2];
                    try {
                        Object acceptable = m.invoke(exe, pars);
                        return acceptable;
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).setArgs(new Object[] { acceptList.get(i).method, obj, params });
                container.accept(name, unit);
                continue nextAcceptable;
            }

            try {
                Object acceptable = acceptList.get(i).method.invoke(obj, params);
                if (acceptable != null) {
                    if (acceptInfo.value().length() > 0) {
                        container.accept(acceptInfo.value(), acceptable);
                    } else {
                        container.accept(acceptList.get(i).method.getReturnType().getName(), acceptable);
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String m = Strings.template("{}.{}(..)", factories[i].getName(), acceptList.get(i).method.getName());
                LOG.warn("Object Creater {} error, losed. Cause: {}", m, e.getMessage());
            }
        }
        // 此缓存线程到此结束，通知其他缓存线程开始
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * 根据根据时间和列表大小等因素解决缓存依赖
     * 
     * @param size
     * @param startTime
     * @throws InterruptedException
     */
    private void resolveDependence(int size, long startTime) throws InterruptedException {
        if (assertShouldWaiting(size, 1000, startTime)) {
            try {
                LOG.debug("Waiting for other cache thread.");
                synchronized (lock) {
                    lock.wait(200);
                }
            } catch (InterruptedException e) {
            }
        }
        if (assertShouldWaiting(size, 10000, startTime)) {
            LOG.error("构建缓存对象过程中出现了循环引用！终止缓存器！");
            throw new InterruptedException();
        }
    }

    /**
     * 根据时间和列表大小等因素确定是否需要等待其他缓存线程
     * 
     * @param currentSize
     * @param targetSize
     * @param startTime
     * @return
     */
    private boolean assertShouldWaiting(int currentSize, int targetSize, long startTime) {
        short it1 = 10;
        short it2 = 20;
        if (currentSize > targetSize) {
        } else if (currentSize > targetSize) {
            it1 = 110;
            it2 = 210;
        }
        return System.currentTimeMillis() - startTime < (Runtime.getRuntime().availableProcessors() >= 2 ? it1 : it2);
    }

    private void createSingletonsByClasses(Container container) {
        List<Class<?>> classList = new ArrayList<>();
        for (int i = 0; i < classes.length; i++) {
            classList.add(classes[i]);
        }
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < classList.size(); i++) {
            try {
                resolveDependence(classList.size(), startTime);
            } catch (InterruptedException e) {
                break;
            }
            if (container.singleton(classList.get(i)) != null) {
                continue; // 已有该类单例，跳过，继续下一个
            }
            if (Injections.suitableFor(classList.get(i), container)) {
                try {
                    Object obj = classList.get(i).newInstance();
                    container.accept(Injections.inject(obj, container));
                } catch (InstantiationException | IllegalAccessException e) {
                    LOG.warn("Class {} is not suitable to cache, losed.", classList.get(i).getName());
                }
            } else { // 稍后缓存
                classList.add(classList.get(i));
            }
        }
        // 此缓存线程到此结束，通知其他缓存线程开始
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
