package net.apisp.quick.http.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServletRequest;

import net.apisp.quick.http.HttpRequest;

public class ServletRequestAdapter implements HttpRequest {

    private HttpServletRequest request;

    public ServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String method() {
        return request.getMethod();
    }

    @Override
    public String uri() {
        return request.getRequestURI();
    }

    @Override
    public String header(String key) {
        return request.getHeader(key);
    }

    @Override
    public byte[] body() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 10);
        InputStream in = null;
        try {
            in = request.getInputStream();
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = in.read(buf)) != -1) {
                byteBuffer.put(buf, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    in = null;
                }
            }
        }
        return byteBuffer.array();
    }

    @Override
    public String version() {
        return "1.1";
    }

    @Override
    public boolean normative() {
        return true;
    }

}
