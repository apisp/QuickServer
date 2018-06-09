package net.apisp.quick.server.simple;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.apisp.quick.http.HttpMethods;
import net.apisp.quick.http.HttpRequest;
import net.apisp.quick.log.Logger;

public class SimpleHttpRequest implements HttpRequest {

    private static final Logger LOGGER = Logger.get(SimpleHttpRequest.class);
    Map<String, String> headers = new HashMap<>();
    String method;
    String uri;
    String version;
    byte[] body;

    public SimpleHttpRequest(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        int posi = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 10);
        while ((posi = is.read(b)) != -1) {
            byteBuffer.put(b, 0, posi);
        }
        byte[] allbytes = byteBuffer.array();
        String allData = new String(allbytes, "utf8");
        String splitor = "\n\n";
        String headerSplitor = "\n";
        if (allData.contains("\r\n")) {
            splitor = "\r\n\r\n";
            headerSplitor = "\r\n";
        }
        String[] headerAndBody = allData.split(splitor);
        body = headerAndBody[1].getBytes("utf8");
        String[] headers = headerAndBody[0].split(headerSplitor);
        parseRequestLine(headers[0]);
        for (int i = 1; i < headers.length; i++) {
            parseRequestHeader(headers[i]);
        }
        LOGGER.info("%s", headerAndBody[0]);

    }

    private void parseRequestLine(String str) {
        if (str == null) {
            return;
        }
        String[] split = str.split("\\s+");
        method = HttpMethods.valueOf(split[0]);
        uri = split[1];
        version = split[2];
    }

    private void parseRequestHeader(String str) {
        String[] kv = str.split(":");
        if (kv.length < 2) {
            LOGGER.info("Lose header: %s", str);
            return;
        }
        headers.put(kv[0].trim(), kv[1].trim());
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String header(String key) {
        return headers.get(key);
    }

    @Override
    public byte[] body() {
        return body;
    }
}
