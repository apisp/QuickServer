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

import java.util.Iterator;
import java.util.Set;

/**
 * @author Ujued
 * @date 2018-06-23 16:38:46
 */
public abstract class Strings {
    
    /**
     * #{null} 或者 "" 返回真
     * @param string
     * @return
     */
    public static final boolean isEmpty(String string) {
        return string == null ? true : (string.equals("") ? true : false);
    }

    /**
     * 集合元素的字符串以半角 ", " 隔开组合成一个字符串返回
     * @param objs
     * @return
     */
    public static final String valueOf(Set<?> objs) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = objs.iterator();
        Object obj;
        while (iter.hasNext()) {
            obj = (Object) iter.next();
            sb.append(obj.toString()).append(", ");
        }
        return sb.toString();
    }
}
