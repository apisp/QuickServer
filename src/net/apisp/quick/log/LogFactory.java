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
import net.apisp.quick.log.Log.Levels;
import net.apisp.quick.server.var.ServerContext;

/**
 * @author UJUED
 * @date 2018-06-13 12:59:44
 */
public class LogFactory {
    private static final Log LOG = new ConsoleLog(Levels.INFO, LogFactory.class.getName());

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static Log getLog(String logName) {
        ServerContext serverContext = ServerContext.tryGet();
        Class<? extends Log> logClass = null;
        if (serverContext != null) {
            logClass = (Class<? extends Log>) serverContext.singleton("logging.class");
            if (logClass == null) {
                try {
                    logClass = (Class<? extends Log>) Class.forName((String) Configuration.get("logging.class"));
                } catch (ClassNotFoundException e) {
                    LOG.warn("Log [%s] non exsits, default used.", Configuration.get("logging.class"));
                }
            }
        }
        Log log = null;
        if (logClass == null) {
            log = new ConsoleLog((String) Configuration.get("logging.level"), logName);
        } else {
            try {
                log = logClass.newInstance();
                log.setLevel((String) Configuration.get("logging.level"));
                log.setName(logName);
            } catch (InstantiationException | IllegalAccessException e) {
                log = new ConsoleLog((String) Configuration.get("logging.level"), logName);
                LOG.warn("Log [%s] not suitable, default used.", Configuration.get("logging.class"));
            }
        }
        return log.normalize();
    }
}
