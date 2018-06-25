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
package net.apisp.quick.config;

import java.util.Iterator;

import net.apisp.quick.util.Classpaths;
import net.apisp.quick.util.Quicks;

public abstract class Configuration {
    private static DefaultConfig configuration = new DefaultConfig();
    static {
        if (Classpaths.existFile("quick.properties")) {
            Configuration tmp = new PropertiesConfig();
            Iterator<KeyValuePair> iterator = tmp.iterator();
            KeyValuePair kv = null;
            while (iterator.hasNext()) { // 覆盖默认配置
                kv = (KeyValuePair) iterator.next();
                configuration.configs.put((String) kv.getKey(),
                        Quicks.transform(configuration.configs.get(kv.getKey()), kv.getValue()));
            }
        }
    }

    public static Object get(String key) {
        return configuration.getValue(key);
    }

    public abstract Object getValue(String key);

    public abstract Iterator<KeyValuePair> iterator();

    public static void applySystemArgs(String... args) {
        for (String setting : args) {
            String[] kv = setting.split("=");
            if (kv.length != 2) {
                System.err.println("System args have a unstandard config, discarded!");
                continue;
            }
            configuration.configs.put(kv[0].trim(), Quicks.transform(configuration.configs.get(kv[0].trim()), kv[1]));
        }
    }
}
