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

import net.apisp.quick.ioc.annotation.Autowired;

/**
 * IOC自动注入工具
 * 
 * @author Ujued
 * @date 2018-06-22 11:42:45
 */
public abstract class Injections {
    public static Object inject(Object obj, Container container) {
        Field[] fields = obj.getClass().getDeclaredFields();
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
        return obj;
    }
}
