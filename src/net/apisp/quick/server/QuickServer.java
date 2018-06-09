package net.apisp.quick.server;

import java.util.Objects;

import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.ServerContext;
import net.apisp.quick.log.Logger;
import net.apisp.quick.server.simple.SimpleServer;
import net.apisp.quick.util.Safes;

public abstract class QuickServer {
    public static final Logger LOGGER = Logger.get(QuickServer.class);
    protected ServerContext serverContext;
    private static QuickServer server;

    /**
     * 选择一个Server实现
     *
     * @return
     */
    public static synchronized QuickServer chose() {
        if (server == null) {
            Class<QuickServer> serverClass = Safes.loadClass(Configuration.get("quick.server").toString(),
                    QuickServer.class);
            if (Objects.nonNull(serverClass)) {
                try {
                    server = serverClass.newInstance();
                    LOGGER.info("Chosed Server %s", serverClass);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.error("自定义Server需要有无参数构造！");
                    server = new SimpleServer();
                }
            } else {
                server = new SimpleServer();
                LOGGER.info("Chosed default Server.");
            }
        }
        return server;
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                server.run();
            }
        }.start();
    }

    public void setContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public abstract void run();
}
