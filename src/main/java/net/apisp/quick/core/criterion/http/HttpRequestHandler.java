package net.apisp.quick.core.criterion.http;

public interface HttpRequestHandler {

    void handle(HttpRequestContext requestContext, HttpRequestParser requestParser);
}
