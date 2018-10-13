package net.apisp.quick.core.criterion.http;

public interface HttpConnectionKeeper {
    void start() throws Exception;

    void keepConnection(HttpConnection connection);
}
