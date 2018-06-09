/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.QuickServer;

public class Quick {
	private static QuickServer server = QuickServer.chose();
	private static final Logger LOGGER = Logger.get(Quick.class);
	private static final ServerContext SERVER_CONTEXT = Quick.context();

	public static ServerContext run(Class<?>... classes) {
		MappingResolver.prepare(classes).resolve(SERVER_CONTEXT);
		server.setContext(SERVER_CONTEXT);
		server.start();
		LOGGER.show("Started Quick API Server on port (%s)", Configuration.get("quick.server.port"));
		return SERVER_CONTEXT;
	}

	public static final ServerContext context() {
		return ServerContext.instance();
	}
}
