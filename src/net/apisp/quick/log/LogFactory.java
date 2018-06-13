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
package net.apisp.quick.log;

import net.apisp.quick.config.Configuration;

/**
 * @author UJUED
 * @date 2018-06-13 12:59:44
 */
public class LogFactory {
    public static Log getLog(Class<?> clazz) {
        // TODO chose a Logger
        return new ConsoleLog((String) Configuration.get("logging.level"), clazz.getName());
    }
}
