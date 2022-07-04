package com.rpc.transport.netty.server;

import com.rpc.codec.CommonDecoder;
import com.rpc.codec.CommonEncoder;
import com.rpc.hook.ShutdownHook;
import com.rpc.provider.ServiceProviderImpl;
import com.rpc.registry.NacosServiceRegistry;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.AbstractRpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:24
 * @description:
 **/
public class NettyServer extends AbstractRpcServer {


    private final CommonSerializer serializer;

    public NettyServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        // nacos注册中心，服务注册表
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        // 添加序列化类型
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    /**
     * 启动方法
     */
    @Override
    public void start() {

        // 关闭之前注册的服务和nacos服务
        ShutdownHook.getShutdownHook().addClearAllHook();

        // 创建连接线程和工作线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // 添加连接线程和工作线程
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup);

        // 添加NioServerSocketChannel和日志打印器
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        /** 存放已完成三次连接的最大长度， 启用心跳保活机制 **/
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 256);
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        //Nagle算法是将小的数据包组装为更大的帧然后进行发送，而不是输入一次发送一次
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);


        try {
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 获取pipline
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    // 添加心跳处理器
                    pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                    // 添加编码器
                    pipeline.addLast(new CommonEncoder(serializer));
                    // 添加解码器
                    pipeline.addLast(new CommonDecoder());
                    // 添加日志打印器
                    pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                    // 添加nettyServerHandler
                    pipeline.addLast(new NettyServerHandler());
                }
            });
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        }finally {
            // 优雅的关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
