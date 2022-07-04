package com.rpc.transport.netty.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.factory.SingletonFactory;
import com.rpc.loadbalancer.LoadBalancer;
import com.rpc.loadbalancer.RandomLoadBalancer;
import com.rpc.registry.NacosServiceDiscovery;
import com.rpc.registry.ServiceDiscovery;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:16
 * @description: NIO方式消费侧客户端类
 **/
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /** 创建工作组，和启动服务器 **/
    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    /** 服务发现 和序列化类型 **/
    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;

    private final UnprocessedRequests unprocessedRequests;

    /**
     * 创建客户端默认使用随机算法
     */
    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    /**
     * 指定负载均衡算法
     * @param loadBalancer
     */
    public NettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    /**
     * 指定序列化的方式
     * @param serializer
     */
    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    /**
     * 指定序列化的方式 负载均衡算法
     * @param serializer
     * @param loadBalancer
     */
    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) throws InterruptedException {

        // 如果没有设置序列化方式报异常
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // 获取线程池中得到的返回结果
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

        try {
            // 获取服务，然后添加客户端
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);

            // 返回channel不为空说明和服务端建立了连接
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }
}
