/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.util;

public abstract class Strings {
    public static final boolean isEmpty(String string) {
        return string == null ? true : (string.equals("") ? true : false);
    }
}
