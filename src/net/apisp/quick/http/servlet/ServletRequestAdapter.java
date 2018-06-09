package net.apisp.quick.http.servlet;

import javax.servlet.http.HttpServletRequest;

import net.apisp.quick.http.HttpRequest;

public class ServletRequestAdapter implements HttpRequest {

    private HttpServletRequest request;

    public ServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String method() {
        return request.getMethod();
    }

    @Override
    public String uri() {
        return request.getRequestURI();
    }

    @Override
    public String header(String key) {
        return request.getHeader(key);
    }

    @Override
    public byte[] body() {
        return null;
    }

}
