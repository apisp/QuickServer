package net.apisp.quick.core.criterion;

public interface QuickServer<CTX> {

    CTX boot(Object... args);
}
