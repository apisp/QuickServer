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

import net.apisp.quick.ioc.Container.Injections;
import net.apisp.quick.ioc.Container.ObjectCreaterUnit;
import net.apisp.quick.ioc.annotation.Accept;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.util.Strings;

/**
 * @author Ujued
 * @date 2018-06-26 21:12:22
 */
public class FactoryResolver {
    private static final Log LOG = LogFactory.getLog(FactoryResolver.class);
    private Class<?>[] factories;
    private Container container;

    private FactoryResolver(Class<?>[] factories, Container container) {
        this.factories = factories;
        this.container = container;
    }

    public static final FactoryResolver prepare(Class<?>[] factories, Container container) {
        return new FactoryResolver(factories, container);
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

    public void resolve() {
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
                        container.accept(name, ObjectCreaterUnit.create((args) -> {
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
                        }, methods[j], obj, params));
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
                        LOG.warn("Class {} not cached. Cause: {}", obj.getClass().getName(), e.getCause().getMessage());
                    }
                }
            }
        }
        // lazyAccept
        nextAcceptable: for (int i = 0; i < acceptList.size(); i++) {
            if (acceptList.size() > 10000) {
                LOG.warn("构建缓存对象过程中出现了循环引用！终止缓存器！");
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
                container.accept(name, ObjectCreaterUnit.create((args) -> {
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
                }, acceptList.get(i).method, obj, params));
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
    }
}
