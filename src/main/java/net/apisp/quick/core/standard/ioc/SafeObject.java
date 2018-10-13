package net.apisp.quick.core.standard.ioc;

import net.apisp.quick.annotation.Nullable;
import net.apisp.quick.old.server.ServerContext;

public class SafeObject<T> {
    @Nullable
    private ThreadLocal<T> threadLocal;
    private String name;

    public SafeObject(String name, ThreadLocal<T> t) {
        this.threadLocal = t;
        this.name = name;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public T get() {
        T r = null;
        if (threadLocal == null) {
            return null;
        }
        if (threadLocal.get() == null) {
            r = (T) ServerContext.tryGet().singleton(name, true);
        } else {
            r = threadLocal.get();
        }
        return r;
    }
}
