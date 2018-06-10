/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core.ase;

import java.util.concurrent.ExecutorService;

import net.apisp.quick.core.WebContext;
import net.apisp.quick.server.ServerContext;

public class QuickWebContext implements WebContext {

    private ServerContext serverContext;

    public QuickWebContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public Object setting(String key) {
        return serverContext.getSetting(key);
    }

    @Override
    public <T> T singleton(Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutorService exeutor() {
        return serverContext.executor();
    }

}
