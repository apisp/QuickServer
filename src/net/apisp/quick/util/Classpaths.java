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
package net.apisp.quick.util;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class Classpaths {

    /**
     * 尝试从类路径加载类，若没有则返回 null
     * 
     * @param classname
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> safeLoadClass(String classname, Class<T> clazz) {
        Class<T> rClass = null;
        try {
            rClass = (Class<T>) Classpaths.class.getClassLoader().loadClass(classname);
        } catch (ClassNotFoundException e) {
            try {
                rClass = (Class<T>) ClassLoader.getSystemClassLoader().loadClass(classname);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
        return rClass;
    }

    public static boolean existFile(String filename) {
        if (filename.charAt(0) == '/') {
            filename = filename.substring(1);
        }
        return Classpaths.class.getResource("/" + filename) == null ? false : true;
    }

    public static Path get(String file) throws FileNotFoundException {
        try {
            URL url = ClassLoader.getSystemResource(file);
            if (url == null) {
                throw new FileNotFoundException(file);
            }
            return Quicks.tactfulPath(url.toURI());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException(file);
        }
    }
}
