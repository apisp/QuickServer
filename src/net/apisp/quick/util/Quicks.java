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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
}
