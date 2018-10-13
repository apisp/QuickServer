package net.apisp.quick.core.criterion.http;

import net.apisp.quick.core.criterion.function.Executor;

public interface HttpRequestContext {

    Executor executor(String method, String uri);

    Executor executor();

    HttpRequest httpRequest();

    HttpResponse httpResponse();
}
