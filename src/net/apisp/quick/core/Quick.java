/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.QuickServer;

/**
 * 框架万能帮助类
 * 
 * @author UJUED
 * @date 2018-06-08 10:34:37
 */
public class Quick {
	private static QuickServer server = QuickServer.chose();
	private static final Logger LOGGER = Logger.get(Quick.class);
	private static final ServerContext SERVER_CONTEXT = Quick.context();

	public static ServerContext run(Class<?>... classes) {
		MappingResolver.prepare(classes).resolve(SERVER_CONTEXT);
		server.setContext(SERVER_CONTEXT);
		server.start();
		LOGGER.show("Started Quick API Server on port (%s)", Configuration.get("server.port"));
		return SERVER_CONTEXT;
	}

	public static final ServerContext context() {
		return ServerContext.instance();
	}
}
