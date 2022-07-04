package com.rpc.transport.socket.server;

import com.rpc.factory.ThreadPoolFactory;
import com.rpc.handler.RequestHandler;
import com.rpc.hook.ShutdownHook;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.NacosServiceRegistry;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.AbstractRpcServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:45
 * @description:
 **/
public class SocketServer  extends AbstractRpcServer {

    private final ExecutorService threadPool;
    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();

    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        // 服务注册实例
        this.serviceRegistry = new NacosServiceRegistry();
        // 服务提供实例
        this.serviceProvider = new ServiceProviderImpl();
        // 序列化方法
        this.serializer = CommonSerializer.getByCode(serializer);
        // 扫描服务
        scanServices();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }

}
