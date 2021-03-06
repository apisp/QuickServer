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
 * 反射工具集合
 * 
 * @author Ujued
 * @date 2018-06-25 18:28:21
 */
public abstract class Reflects {
	/**
	 * 反射执行某对象函数
	 * 
	 * @param obj
	 * @param methodName
	 * @param args
	 */
	public static boolean invoke(Object obj, String methodName, Object... args) {
		Class<?> cls = obj.getClass();
		Class<?>[] parameterTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		try {
			Method m = cls.getDeclaredMethod(methodName, parameterTypes);
			m.setAccessible(true);
			m.invoke(obj, args);
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}
	}
}
