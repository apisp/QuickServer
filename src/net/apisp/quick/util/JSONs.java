/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.apisp.quick.log.Logger;

/**
 * JSON工具
 *
 * @date 2018年6月8日 下午2:40:08
 * @author UJUED
 */
public class JSONs {
    private static final Logger lOGGER = Logger.get(JSONs.class);

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
        private List<FieldValue<Double>> numPairs = new ArrayList<>();
        private List<FieldValue<String>> strPairs = new ArrayList<>();
        private List<FieldValue<JSON>> jsonPairs = new ArrayList<>();
        private List<FieldValue<List<?>>> listPairs = new ArrayList<>();

        public void put(String field, Double value) {
            numPairs.add(new FieldValue<Double>(field, value));
        }

        public void put(String field, String value) {
            strPairs.add(new FieldValue<String>(field, value));
        }

        public void put(String field, JSON value) {
            jsonPairs.add(new FieldValue<JSONs.JSON>(field, value));
        }

        public void put(String field, List<?> value) {
            listPairs.add(new FieldValue<List<?>>(field, value));
        }

        @Override
        public String toString() {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append('{');
            for (int i = 0; i < numPairs.size(); i++) {
                jsonBuilder.append('"').append(numPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append(numPairs.get(i).getValue()).append(',').append(' ');
            }
            for (int i = 0; i < strPairs.size(); i++) {
                jsonBuilder.append('"').append(strPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append('"').append(strPairs.get(i).getValue()).append('"').append(',').append(' ');
            }
            for (int i = 0; i < jsonPairs.size(); i++) {
                jsonBuilder.append('"').append(jsonPairs.get(i).getField()).append('"').append(':').append(' ')
                        .append(jsonPairs.get(i).getValue()).append(',').append(' ');
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
                    || fields[i].getType().equals(short.class)) {
                try {
                    jsonStr.put(fields[i].getName(), Double.valueOf(fields[i].get(pojo).toString()));
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
            lOGGER.error("%s 需要无参数构造函数。", type);
        }
        return obj;
    }
}
