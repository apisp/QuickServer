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

import net.apisp.quick.core.Cacheable;

/**
 * @author UJUED
 * @date 2018-06-15 11:35:15
 */
public class InstanceFactory implements Cacheable<Container> {
    private Class<?>[] classes;
    private Object[] instances;

    public InstanceFactory(Class<?>[] classes) {
        this.classes = classes;
    }

    public Object[] instances() {
        instances = classes;
        return instances;
    }
    @Override
    public Container cache(Container container) {
        return null;
    }

}
