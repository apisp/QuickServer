/**
 * Copyright (c) 2018, All Rights Reserved. 
 */
package net.apisp.quick.http;

/**
 * HTTP请求
 *
 * @date 2018年6月8日 上午11:37:45
 * @author ujued 
 */
public interface HttpRequest {
	String method();
	String uri();
	String version();
	String header(String key);
	byte[] body();
	boolean normative();
}
