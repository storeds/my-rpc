package com.rpc.transport.netty.client;

import com.rpc.codec.CommonDecoder;
import com.rpc.codec.CommonEncoder;
import com.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:28
 * @description: 用于获取 Channel 对象
 **/
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    /** 设置工作线程 和客户端启动服务器 **/
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();


    /** 存储客户端的channel **/
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();


    /**
     *
     * @param inetSocketAddress 端口 地址对象
     * @param serializer 序列化算法
     * @return
     * @throws InterruptedException
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) throws InterruptedException {
        // 获取channel的key
        String key = inetSocketAddress.toString() + serializer.getCode();

        // 检查channel的key
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            // 如果channel不等于null，或者在使用返回，否则移除
            if(channels != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }

        // 启动客户端的处理
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                // 获取pipeline
                ChannelPipeline pipeline = socketChannel.pipeline();
                // 先是编码器
                pipeline.addLast(new CommonEncoder(serializer));
                // 添加心跳handler
                pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                // 添加解码器
                pipeline.addLast(new CommonDecoder());
                // 添加client的handler
                pipeline.addLast(new NettyClientHandler());
            }
        });
        Channel channel = null;

        try {
            channel = connect(bootstrap, inetSocketAddress);
        } catch (ExecutionException e) {
            logger.error("连接客户端时有错误发生", e);
            return null;
        }

        channels.put(key, channel);
        return channel;
    }

    /**
     * 连接服务器
     * @param bootstrap  客户端启动器
     * @param inetSocketAddress 端口和地址
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 客户端连接服务器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
           if (future.isSuccess()) {
               logger.info("客户端连接成功!");
               completableFuture.complete(future.channel());
           }else {
               throw new IllegalStateException();
           }
        });
        // 返回结果
        return completableFuture.get();
    }

    /**
     * 初始化客户端的启动器
     * @return
     */
    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

}
