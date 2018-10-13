package net.apisp.quick.core.criterion;

import net.apisp.quick.core.exception.NonConfigurationItemException;

public interface QuickConfiguration {
    <T> T item(String name, Class<T> type) throws NonConfigurationItemException;
}
