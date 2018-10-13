package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpRequestHandler;
import net.apisp.quick.core.criterion.http.HttpResponse;
import net.apisp.quick.core.exception.StopServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketResolver {
    private ThreadPoolExecutor executor;

    public SocketResolver() {
        int coreSize = 300;
        executor = new ThreadPoolExecutor(coreSize, (int) (coreSize * 1.5), 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>((int) (coreSize * 1.2)), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public void adapt(Socket socket, HttpRequestHandler requestHandler, String charset) {
        executor.submit(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                StandardRequestParser requestParser = new StandardRequestParser(inputStream);
                StandardRequestContext requestContext = new StandardRequestContext(null, requestParser, charset);
                requestHandler.handle(requestContext, requestParser);
                OutputStream outputStream = socket.getOutputStream();
                HttpResponse response = requestContext.httpResponse();
                outputStream.write(getBytesByResponse(response));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (StopServerException e) {
                // Stop The Server.
            }
        });
    }

    private byte[] getBytesByResponse(HttpResponse response) throws StopServerException {
        StandardResponse standardResponse = (StandardResponse) response;
        return standardResponse.bytes();
    }

}
