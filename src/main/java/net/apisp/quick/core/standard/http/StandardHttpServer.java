package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpQuickServer;
import net.apisp.quick.core.criterion.http.HttpRequestHandler;
import net.apisp.quick.core.criterion.http.HttpServerContext;
import net.apisp.quick.core.exception.NonConfigurationItemException;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.old.server.http.DefaultQuickServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StandardHttpServer implements HttpQuickServer {

    private static final Log LOG = LogFactory.getLog(DefaultQuickServer.class);

    private SocketResolver socketResolver;

    private HttpRequestHandler requestHandler;

    public StandardHttpServer() {
        socketResolver = new SocketResolver();
    }

    @Override
    public void setRequestHandler(HttpRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void start(HttpServerContext context) {
        StandardConnectionKeeper connectionKeeper = new StandardConnectionKeeper();
        try {
            // 监听端口
            int serverPort = context.configuration().item("server.port", int.class);

            // 字符编码
            String charset = context.configuration().item("server.charset", String.class);

            // 长连接检测队列启动
            connectionKeeper.start();

            // 服务端套接字
            ServerSocket serverSocket = new ServerSocket(serverPort);

            // 循环监听
            while (true) {
                Socket socket = serverSocket.accept();
                LOG.debug("New connection come in.");
                socketResolver.adapt(socket, requestHandler, charset);
            }

        } catch (IOException e) {
            LOG.error("Port in used.");
        } catch (NonConfigurationItemException e) {
            LOG.error("Configuration item.");
        } catch (Exception e) {
            LOG.error("Error.");
        }
    }

}
