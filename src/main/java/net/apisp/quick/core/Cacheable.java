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
package net.apisp.quick.core;

/**
 * 所有可以缓存的对象都要继承这个接口
 * 
 * @author UJUED
 * @date 2018-06-13 14:21:18
 */
public interface Cacheable<T> {
    
    /**
     * 开始缓存到给定容器，并返回该容器
     * 
     * @param obj 缓存容器
     * @return 缓存容器
     */
    T cache(T obj);
}
