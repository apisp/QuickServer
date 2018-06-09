/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.http;

/**
 * HTTP请求
 *
 * @author UJUED 
 * @date 2018-6-8 11:37:45
 */
public interface HttpRequest {
	String method();
	String uri();
	String version();
	String header(String key);
	byte[] body();
	boolean normative();
}
