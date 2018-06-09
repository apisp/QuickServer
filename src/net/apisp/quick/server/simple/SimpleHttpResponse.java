package net.apisp.quick.server.simple;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.apisp.quick.core.RequestExecutorInfo;
import net.apisp.quick.core.RequestProcessor;
import net.apisp.quick.core.RequestProcessor.ResponseInfo;
import net.apisp.quick.core.ServerContext;

public class SimpleHttpResponse {
    // private static Logger LOGGER = Logger.get(HTTPResponse.class);
    private String version;
    private int status;
    List<String> headers = new ArrayList<String>();
    byte[] body;

    public SimpleHttpResponse(SimpleHttpRequest req, ServerContext serverContext) throws IOException {
        this.version = req.version;
        RequestExecutorInfo executeInfo = serverContext.hit(req.method, req.uri);
        fillHeaders();
        ResponseInfo responseInfo = RequestProcessor.create(executeInfo).process(req);
        this.body = responseInfo.getBody();
        this.status = responseInfo.getStatusCode();
        headers.add(responseInfo.getContentType());
    }

    private void fillHeaders() {
        headers.add(version + " " + status);
        headers.add("Server: QuickSimpleServer");
        headers.add("Access-Control-Allow-Origin: *");
        headers.add("Access-Control-Allow-Methods: GET");
    }

    public void write(OutputStream os) throws IOException {
        DataOutputStream output = new DataOutputStream(os);
        for (String header : headers) {
            output.writeBytes(header + "\r\n");
        }
        output.writeBytes("\r\n");
        if (body != null) {
            output.write(body);
        }
        output.writeBytes("\r\n");
        output.flush();
    }
}
