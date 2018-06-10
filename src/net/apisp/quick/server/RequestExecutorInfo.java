/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.server;

import java.lang.reflect.Method;

/**
 * API触发的事件执行信息
 * @author UJUED
 * 2018年6月8日 上午11:20:09
 */
public class RequestExecutorInfo {
	private Method method;
	private Object object;
	private String responseType;

	public RequestExecutorInfo() {}

	public RequestExecutorInfo(Method method, Object object, String responseType) {
		this.method = method;
		this.object = object;
		this.responseType = responseType;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

}
