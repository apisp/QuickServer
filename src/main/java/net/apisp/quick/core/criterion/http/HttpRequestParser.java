package net.apisp.quick.core.criterion.http;

public interface HttpRequestParser {

    HttpRequest httpRequest();

    Object[] intelliRouteArgs();
}
