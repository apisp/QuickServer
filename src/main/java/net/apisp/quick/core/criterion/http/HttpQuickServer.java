package net.apisp.quick.core.criterion.http;

public interface HttpQuickServer {

    void setRequestHandler(HttpRequestHandler requestHandler);

    void start(HttpServerContext context);

    default void handleRequest(HttpRequestContext context, HttpRequestParser httpRequestParser) {
        context.executor().execute(httpRequestParser.intelliRouteArgs());
    }
}
