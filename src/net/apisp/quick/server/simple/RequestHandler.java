package net.apisp.quick.server.simple;

import java.net.Socket;

import net.apisp.quick.core.ServerContext;

public class RequestHandler implements Runnable {
	private ServerContext serverContext;
	private Socket socket;

	public RequestHandler(ServerContext serverContext, Socket socket) {
		this.socket = socket;
		this.serverContext = serverContext;
	}

	public void run() {
		try {
			SimpleHttpRequest req = new SimpleHttpRequest(socket.getInputStream());
			if (req.method != null) {
				SimpleHttpResponse res = new SimpleHttpResponse(req, serverContext);
				res.write(socket.getOutputStream());
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
