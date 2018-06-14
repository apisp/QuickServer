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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * JSON工具
 *
 * @date 2018年6月8日 下午2:40:08
 * @author UJUED
 */
public class JSONs {
    private static final Log LOG = LogFactory.getLog(JSONs.class);

    public static class FieldValue<T> {
        private String field;
        private T value;

        public FieldValue(String field, T value) {
            super();
            this.field = field;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

    }

    public static class JSON {
        private List<FieldValue<Object>> objPairs = new ArrayList<>();
        private List<FieldValue<String>> strPairs = new ArrayList<>();
        private List<FieldValue<List<?>>> listPairs = new ArrayList<>();

        public void put(String field, Object value) {
            objPairs.add(new FieldValue<Object>(field, value));
        }

        public void put(String field, String value) {
            strPairs.add(new FieldValue<String>(field, value));
        }

        public void put(String field, List<?> value) {
            listPairs.add(new FieldValue<List<?>>(field, value));
        }

        @Override
        public String toString() {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append('{');
            for (int i = 0; i < objPairs.size(); i++) {
                jsonBuilder.append('"').append(objPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append(objPairs.get(i).getValue()).append(',').append(' ');
            }
            for (int i = 0; i < strPairs.size(); i++) {
                jsonBuilder.append('"').append(strPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append('"').append(strPairs.get(i).getValue()).append('"').append(',').append(' ');
            }
            for (int i = 0; i < listPairs.size(); i++) {
                jsonBuilder.append('"').append(listPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append('[');
                List<?> l = listPairs.get(i).getValue();
                for (int j = 0; j < l.size(); j++) {
                    Class<?> pojoClass = l.get(j).getClass();
                    if (pojoClass.equals(Integer.class) || pojoClass.equals(int.class) || pojoClass.equals(Double.class)
                            || pojoClass.equals(Double.class) || pojoClass.equals(double.class)
                            || pojoClass.equals(Short.class) || pojoClass.equals(short.class)) {
                        jsonBuilder.append(l.get(j));
                    } else if (pojoClass.equals(String.class) || pojoClass.equals(Date.class)) {
                        jsonBuilder.append('"').append(l.get(j)).append('"');
                    } else if (pojoClass.equals(List.class)) {
                    } else {
                        jsonBuilder.append(convert(l.get(j)));
                    }
                    jsonBuilder.append(',').append(' ');
                }
                jsonBuilder.delete(jsonBuilder.length() - 2, jsonBuilder.length());
                jsonBuilder.append(']');
                jsonBuilder.append(',').append(' ');
            }
            jsonBuilder.delete(jsonBuilder.length() - 2, jsonBuilder.length());
            jsonBuilder.append('}');
            return jsonBuilder.toString();
        }

    }

    public static final String convert(Object pojo) {
        JSON json = parse(pojo);
        return json == null ? null : json.toString();
    }

    private static final JSON parse(Object pojo) {
        JSON jsonStr = new JSON();
        Class<?> pojoClass = pojo.getClass();
        if (pojoClass.equals(Integer.class) || pojoClass.equals(int.class) || pojoClass.equals(Double.class)
                || pojoClass.equals(Double.class) || pojoClass.equals(double.class) || pojoClass.equals(Short.class)
                || pojoClass.equals(short.class)) {
        } else if (pojoClass.equals(String.class) || pojoClass.equals(Date.class)) {
            return null;
        } else if (pojoClass.equals(List.class) || List.class.isAssignableFrom(pojoClass)) {
            JSON json = new JSON();
            json.put("list", (List<?>) pojo);
            return json;
        }
        Field[] fields = pojoClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (fields[i].getType().equals(Integer.class) || fields[i].getType().equals(int.class)
                    || fields[i].getType().equals(Double.class) || fields[i].getType().equals(Double.class)
                    || fields[i].getType().equals(double.class) || fields[i].getType().equals(Short.class)
                    || fields[i].getType().equals(short.class) || fields[i].getType().equals(Boolean.class)
                    || fields[i].getType().equals(boolean.class)) {
                try {
                    jsonStr.put(fields[i].getName(), fields[i].get(pojo));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (fields[i].getType().equals(String.class) || fields[i].getType().equals(Date.class)) {
                try {
                    jsonStr.put(fields[i].getName(), fields[i].get(pojo).toString());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (fields[i].getType().equals(List.class)) {
                try {
                    jsonStr.put(fields[i].getName(), (List<?>) fields[i].get(pojo));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    jsonStr.put(fields[i].getName(), parse(fields[i].get(pojo)));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonStr;
    }

    public static final <T> T convert(String pojo, Class<T> type) {
        if (type == null) {
            return null;
        }
        T obj = null;
        try {
            obj = type.newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("%s 需要无参数构造函数。", type);
        }
        return obj;
    }
}
