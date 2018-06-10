/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core.type;

import net.apisp.quick.server.ServerContext;

@FunctionalInterface
public interface QuickServerRunner {
    void run(ServerContext serverContext) throws Exception;
}
