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
package net.apisp.quick.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.util.Strings;

/**
 * @author Ujued
 * @date 2018-06-23 23:10:52
 */
public class T {
    private Map<String, Object> vars;

    private T() {
    }

    public static T newT() {
        return new T();
    }

    public T setVariables(Map<String, Object> vars) {
        this.vars = vars;
        return this;
    }

    public String render(Path thtml) {
        try {
            byte[] file = Files.readAllBytes(thtml);
            return Strings.template(new String(file, (String) Configuration.get("charset")),
                    Optional.ofNullable(vars).orElse(new HashMap<>()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.vars = null;
        }
        return null;
    }
}
