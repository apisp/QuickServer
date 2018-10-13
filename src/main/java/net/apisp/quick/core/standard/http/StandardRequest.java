package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpRequest;
import net.apisp.quick.old.server.std.BodyBinary;

public class StandardRequest implements HttpRequest {
    @Override
    public String method() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public String params() {
        return null;
    }

    @Override
    public String version() {
        return null;
    }

    @Override
    public String header(String key) {
        return null;
    }

    @Override
    public StandardHttpCookie cookie(String key) {
        return null;
    }

    @Override
    public StandardHttpCookie[] cookies() {
        return new StandardHttpCookie[0];
    }

    @Override
    public BodyBinary body() {
        return null;
    }

    @Override
    public boolean normative() {
        return false;
    }

    @Override
    public String ip() {
        return null;
    }

    @Override
    public Object variable(String variable) {
        return null;
    }

    @Override
    public <T> T variable(String variable, Class<T> type) {
        return null;
    }
}
