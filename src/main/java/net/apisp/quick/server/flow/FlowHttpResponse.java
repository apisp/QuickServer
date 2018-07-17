package net.apisp.quick.server.flow;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

public class FlowHttpResponse implements FlowResponse {
	public static final Log LOG = LogFactory.getLog(FlowHttpResponse.class);

	private SocketAndOutputStream sAndO;
	
	public FlowHttpResponse(){
		this(SocketAndOutputStream.current());
	}
	
	private FlowHttpResponse(SocketAndOutputStream sAndO) {
	    sAndO.setStream(true);
		this.sAndO = sAndO;
		ByteBuffer responseData = ByteBuffer.allocate(1024 * 100);
		responseData.put(String.format("HTTP/1.1 %d %s", 200, "OK").getBytes());
		responseData.put("\r\n".getBytes());
		responseData.put(("Server: QuickServer/1.0").getBytes());
		responseData.put("\r\n".getBytes());
		responseData.put("Content-Type: application/stream+json".getBytes());
		responseData.put("\r\n\r\n".getBytes());
		responseData.flip();
		byte[] data = new byte[responseData.limit()];
		
		responseData.get(data);
		try {
			sAndO.getOutputStream().write(data);
			sAndO.getOutputStream().flush();
		} catch (IOException e) {
		}
	}

	@Override
	public FlowResponse append(byte[] content) {
		try {
			sAndO.getOutputStream().write(content);
			sAndO.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	@Override
	public void over() {
		try {
			sAndO.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
