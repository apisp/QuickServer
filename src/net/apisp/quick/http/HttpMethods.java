package net.apisp.quick.http;

public abstract class HttpMethods {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String valueOf(String method){
        return method.toUpperCase();
    }
}
