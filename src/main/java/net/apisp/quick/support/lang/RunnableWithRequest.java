package net.apisp.quick.support.lang;

import net.apisp.quick.core.http.HttpRequest;

@FunctionalInterface
public interface RunnableWithRequest {
	void run(HttpRequest req);
}
