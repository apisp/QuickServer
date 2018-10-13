package net.apisp.quick.core.criterion.function;

@FunctionalInterface
public interface Executor {
    Object execute(Object... args);
}
