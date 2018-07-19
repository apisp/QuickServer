package net.apisp.quick.server.http.flow;

import java.io.OutputStream;
import java.net.Socket;

public class SocketAndOutputStream {
	public static final ThreadLocal<SocketAndOutputStream> CURRENT_SO = new ThreadLocal<>();
	private Socket socket;
	private OutputStream outputStream;
	private boolean stream;
	
	public SocketAndOutputStream(Socket sock, OutputStream out) {
		this.socket = sock;
		this.outputStream = out;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	public static SocketAndOutputStream current() {
		return CURRENT_SO.get();
	}
    public boolean isStream() {
        return stream;
    }
    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
