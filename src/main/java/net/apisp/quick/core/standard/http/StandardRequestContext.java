package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.function.Executor;
import net.apisp.quick.core.criterion.http.HttpRequest;
import net.apisp.quick.core.criterion.http.HttpRequestContext;
import net.apisp.quick.core.criterion.http.HttpRequestParser;
import net.apisp.quick.core.criterion.http.HttpResponse;

import java.util.Map;

public class StandardRequestContext implements HttpRequestContext {

    private String method;

    private String uri;

    private Map<String, Executor> executorMap;

    private HttpResponse httpResponse;

    private HttpRequest httpRequest;

    public StandardRequestContext(Map<String, Executor> executorMap, HttpRequestParser requestParser, String charset) {
        this.executorMap = executorMap;
        this.method = requestParser.httpRequest().method();
        this.uri = requestParser.httpRequest().uri();

        this.httpResponse = new StandardResponse(charset);
        this.httpRequest = requestParser.httpRequest();
    }

    @Override
    public Executor executor(String method, String uri) {
        return executorMap.get(method + " " + uri);
    }

    @Override
    public Executor executor() {
        return executor(this.method, this.uri);
    }

    @Override
    public HttpRequest httpRequest() {
        return httpRequest;
    }

    @Override
    public HttpResponse httpResponse() {
        return httpResponse;
    }
}
