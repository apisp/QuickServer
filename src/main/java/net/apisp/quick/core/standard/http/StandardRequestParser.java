package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpRequest;
import net.apisp.quick.core.criterion.http.HttpRequestParser;

import java.io.InputStream;

public class StandardRequestParser implements HttpRequestParser {

    private HttpRequest httpRequest;

    public StandardRequestParser(InputStream inputStream) {

    }

    @Override
    public HttpRequest httpRequest() {
        return httpRequest;
    }

    @Override
    public Object[] intelliRouteArgs() {
        return new Object[0];
    }
}
