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
package net.apisp.quick.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 实验性质的工具
 * 
 * @author Ujued
 * @date 2018-06-22 18:18:58
 */
public abstract class Quicks {
    public static String packageName(String className) {
        if (className.indexOf('.') == -1) {
            return "";
        }
        return className.substring(0, className.lastIndexOf('.'));
    }

    /**
     * 反射执行某对象函数
     * 
     * @param obj
     * @param methodName
     * @param args
     */
    public static void invoke(Object obj, String methodName, Object... args) {
        Class<?> cls = obj.getClass();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        try {
            Method m = cls.getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            m.invoke(obj, args);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据URI获取合适的Path。目前仅支持系统FS和ZIPFS
     * 
     * @param uri
     * @return
     */
    public static Path tactfulPath(URI uri) {
        if (uri.getScheme().equals("jar")) {
            int index = uri.toString().indexOf('!') + 1;
            URI rootUri = URI.create(uri.toString().substring(0, index + 1));
            String filePath = uri.toString().substring(index, uri.toString().length());
            try {
                FileSystem zipfs = FileSystems.newFileSystem(rootUri, new HashMap<>());
                return zipfs.getPath(filePath);
            } catch (FileSystemAlreadyExistsException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Paths.get(uri);
    }

    /**
     * 将${value}转换为${model}的类型
     * 
     * @param model
     * @param value
     * @return
     */
    public static Object transform(Object model, Object value) {
        if (model instanceof Integer) {
            return Integer.valueOf(value.toString());
        } else if (model instanceof String) {
            return value.toString();
        } else if (model instanceof Double) {
            return Double.valueOf(value.toString());
        } else if (model instanceof Boolean) {
            return Boolean.valueOf(value.toString());
        } else if (model instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(value.toString());
            } catch (ParseException e) {
            }
        }
        return value;
    }

    /**
     * 获取Quick Server 版本号
     * @return
     */
    public static String version() {
        String url = Quicks.class.getResource("").toString();
        int i = -1;
        if ((i = url.indexOf('!')) != -1) {
            String name = url.substring(0, i);
            return name.substring(name.lastIndexOf('-') + 1, name.lastIndexOf('.'));
        }
        return "unkonwn";
    }
}
