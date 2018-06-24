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

import net.apisp.quick.ioc.Container.SafeObject;
import net.apisp.quick.ioc.annotation.Autowired;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * IOC自动注入工具
 * 
 * @author Ujued
 * @date 2018-06-22 11:42:45
 */
public abstract class Injections {

    private static final Log LOG = LogFactory.getLog(Injections.class);

    public static Object inject(Object obj, Container container) {
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
                        LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(), obj.getClass().getName());
                    }
                    continue;
                }
                try {
                    String name = autowired.value();
                    if (name.length() == 0) {
                        name = fields[j].getType().getName();
                    }
                    Object v = container.singleton(name);
                    if (v == null) {
                        v = container.singleton(name, true);
                    }
                    fields[j].set(obj, v);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(), obj.getClass().getName());
                }
            }
        }
        return obj;
    }
}
