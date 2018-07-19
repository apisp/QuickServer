package net.apisp.quick.server;

import net.apisp.quick.annotation.ReflectionCall;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * API触发的事件执行信息
 *
 * @author UJUED
 * @date 2018-06-08 11:20:09
 */
public class RequestExecutorInfo {
    /**
     * 正常的处理类型
     */
    public static final Character TYPE_NORMAL = 'a';

    /**
     * 流式处理类型
     */
    public static final Character TYPE_STREAM = 'b';

    private static final Log LOG = LogFactory.getLog(RequestExecutorInfo.class);
    private Method method;
    private Object object;
    private Map<String, String> responseHeaders = new HashMap<>();
    private Map<String, Object> pathVariables = new HashMap<>();
    private List<String> pathVariableNames = new ArrayList<>();
    private String viewDirectory;
    private char type;

    public RequestExecutorInfo() {
    }

    public RequestExecutorInfo(Method method, Object object) {
        this.method = method;
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getViewDirectory() {
        return viewDirectory;
    }

    public void setViewDirectory(String viewDirectory) {
        this.viewDirectory = viewDirectory;
    }

    public RequestExecutorInfo addHeader(String key, String value) {
        this.responseHeaders.put(key, value);
        return this;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void addPathVariableName(String name) {
        this.pathVariableNames.add(name);
    }

    public Object getPathVariable(String key, Class<?> type) {
        Object obj = pathVariables.get(key);
        String c = object.getClass().getName();
        String m = method.getName();
        if (obj == null) {
            return null;
        }
        if (Integer.class.equals(type) || int.class.equals(type)) {
            int intVal = -1;
            try {
                intVal = Integer.valueOf(obj.toString());
            } catch (NumberFormatException e) {
                LOG.warn("At {}.{}. PathVariable [{}] can't format to int.", c, m, key);
            }
            return intVal;
        } else if (Double.class.equals(type) || double.class.equals(type)) {
            double doubleVal = -1.0;
            try {
                doubleVal = Double.valueOf(obj.toString());
            } catch (NumberFormatException e) {
                LOG.warn("At {}.{}. PathVariable [{}] can't format to double.", c, m, key);
            }
            return doubleVal;
        } else if (Long.class.equals(type) || long.class.equals(type)) {
            long longVal = -1;
            try {
                longVal = Long.valueOf(obj.toString());
            } catch (NumberFormatException e) {
                LOG.warn("At {}.{}(). PathVariable [{}] can't format to long.", c, m, key);
            }
            return longVal;
        } else if (Date.class.equals(type)) {
            Date d = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                d = sdf.parse(obj.toString());
            } catch (ParseException e) {
                LOG.warn("At {}.{}. PathVariable [{}] can't format to date.", c, m, key);
            }
            return d;
        } else {
            try {
                return URLDecoder.decode(obj.toString(), ServerContext.tryGet().charset());
            } catch (UnsupportedEncodingException e) {
                return obj;
            }
        }
    }

    public void addPathVariable(String value, int index) {
        this.pathVariables.put(this.pathVariableNames.get(index), value);
    }

    /**
     * 请求执行类型，一般为正常的和流式的
     *
     * @return
     */
    public Character type() {
        return type;
    }

    @ReflectionCall("")
    private void setType(Character type) {
        this.type = type;
    }
}
