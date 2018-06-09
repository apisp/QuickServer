package net.apisp.quick.server.simple;

import java.io.IOException;
import java.net.ServerSocket;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.ServerContext;
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.QuickServer;

public class SimpleServer extends QuickServer {

	private ServerContext serverContext;

	private static Logger LOGGER = Logger.get(SimpleServer.class);

	/* 
	 * @see net.apisp.quick.server.Server#setContext(net.apisp.quick.core.ServerContext)
	 */
	@Override
	public void setContext(ServerContext serverContext) {
		this.serverContext = serverContext;
	}

	/* 
	 * @see net.apisp.quick.server.QuickServer#run()
	 */
	@Override
	public void run() {
		int port = (int) Configuration.get("quick.server.port");
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				serverContext.executor().submit(new RequestHandler(serverContext, serverSocket.accept()));
			}
		} catch (IOException e) {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			LOGGER.error("Error");
		}
	}
}
