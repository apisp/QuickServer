package net.apisp.quick.core.standard.ioc;

import net.apisp.quick.core.criterion.ioc.Container;
import net.apisp.quick.core.criterion.ioc.annotation.Autowired;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

import java.lang.reflect.Field;
import java.util.Objects;

public class Injections {
    private static final Log LOG = LogFactory.getLog(Injections.class);

    public static boolean suitableFor(Class<?> target, Container container) {
        Field[] fields = target.getDeclaredFields();
        Autowired autowired;
        for (int j = 0; j < fields.length; j++) { // 遍历字段
            if ((autowired = fields[j].getAnnotation(Autowired.class)) != null) {
                String name = autowired.value();
                Class<?> safeCls = autowired.safeType();
                if (name.length() == 0) { // 优先使用注解value值
                    name = safeCls.equals(Void.class) ? fields[j].getName() : safeCls.getName();
                }
                if (container.singleton(name) != null || container.safeSingleton(name) != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static <T> T inject(T obj, Container container) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Autowired autowired = null;
        for (int j = 0; j < fields.length; j++) { // 遍历字段
            if ((autowired = fields[j].getAnnotation(Autowired.class)) != null) {
                fields[j].setAccessible(true);
                if (!autowired.safeType().equals(Void.class) && fields[j].getType().equals(SafeObject.class)) {
                    String name = autowired.safeType().getName();
                    SafeObject<?> safe = new SafeObject<>(name, container.safeSingleton(name));
                    try {
                        fields[j].set(obj, safe);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(),
                                obj.getClass().getName());
                    }
                    continue;
                }
                try {
                    String name = autowired.value();
                    if (name.length() == 0) {
                        name = fields[j].getType().getName();
                    }
                    Object v = container.singleton(name);
                    if (Objects.isNull(v)) {
                        if ((v = container.singleton(name, true)) == null) {
                            v = container.setting(name);
                        }
                    }
                    try {
                        fields[j].set(obj, v);
                    } catch (IllegalArgumentException e) {
                        fields[j].set(obj, v.toString());
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOG.warn("Field {} of {} failed set.", fields[j].getType().getName(), obj.getClass().getName());
                }
            }
        }
        return obj;
    }
}
