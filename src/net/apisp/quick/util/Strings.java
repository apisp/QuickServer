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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author Ujued
 * @date 2018-06-23 16:38:46
 */
public abstract class Strings {
    private static final Log LOG = LogFactory.getLog(Strings.class);

    /**
     * #{null} 或者 "" 返回真
     * 
     * @param string
     * @return
     */
    public static final boolean isEmpty(String string) {
        return string == null ? true : (string.equals("") ? true : false);
    }

    /**
     * 集合元素的字符串以半角 ", " 隔开组合成一个字符串返回
     * 
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

    /**
     * 生成UUID
     * 
     * @return
     */
    public static final String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 模板函数。按 {} 和 给定参数 顺序格式化字符串
     * 
     * @param pattern
     * @param args
     * @return
     */
    public static String template(String pattern, Object... args) {
        pattern = Optional.ofNullable(pattern).orElse("null");
        StringBuilder finalStr = new StringBuilder();
        int index = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{' && pattern.charAt(++i) == '}') {
                if (index < args.length) {
                    finalStr.append(Optional.ofNullable(args[index++]).orElse("null").toString());
                } else {
                    finalStr.append("{}");
                }
                continue;
            } else if (c == '{') {
                i--;
            }
            finalStr.append(c);
        }
        return finalStr.toString();
    }

    /**
     * 高级模板。根据${variable}从字典中找到并格式化字符串
     * 
     * @param content
     * @param vars
     * @return
     */
    public static String template(String content, Map<String, Object> vars) {
        if (Objects.isNull(vars)) {
            vars = new HashMap<>();
        }
        Pattern p = Pattern.compile("\\$\\{[^\\{\\}]+?\\}");
        Matcher matcher = p.matcher(content);
        while (matcher.find()) {
            String key = matcher.group().substring(2, matcher.group().length() - 1);
            content = content.replace(matcher.group(), Optional.ofNullable(vars.get(key)).orElse("").toString());
        }
        return content;
    }

    /**
     * 尝试用指定的编码获取字符串的字节数组，系统编码会代替不支持的编码
     * 
     * @param content
     * @param charset
     * @return
     */
    public static byte[] bytes(String content, String charset) {
        if (Objects.isNull(content) || Objects.isNull(charset)) {
            LOG.warn("SafeGetbytes: content or charset is null.");
            return new byte[0];
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return content.getBytes();
        }
    }

    /**
     * 尝试用指定的编码集编码字节数组，系统编码会代替不支持的编码
     * 
     * @param bin
     * @param charset
     * @return
     */
    public static String toString(byte[] bin, String charset) {
        try {
            return new String(bin, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(bin);
        }
    }
}
