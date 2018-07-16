package net.apisp.quick.server.flow;

public interface FlowResponse {
	static FlowResponse ok() {
		FlowResponse flowResponse = new FlowHttpResponse();
		return flowResponse;
	}
	FlowResponse append(byte[] content);
	
	void over();
}
