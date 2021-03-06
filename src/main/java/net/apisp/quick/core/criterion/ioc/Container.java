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
package net.apisp.quick.core.criterion.ioc;

import net.apisp.quick.core.standard.ioc.ObjectInventorUnit;

import java.util.Set;

/**
 * @author UJUED
 * @date 2018-06-15 00:29:29
 */
public interface Container {
    <T> T accept(T obj);

    <T> T accept(String name, T obj);

    void accept(String name, ObjectInventorUnit creater);

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
}
