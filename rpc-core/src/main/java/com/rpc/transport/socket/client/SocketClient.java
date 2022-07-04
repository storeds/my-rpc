package com.rpc.transport.socket.client;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.ResponseCode;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.loadbalancer.LoadBalancer;
import com.rpc.loadbalancer.RandomLoadBalancer;
import com.rpc.registry.NacosServiceDiscovery;
import com.rpc.registry.ServiceDiscovery;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcClient;
import com.rpc.transport.socket.util.ObjectReader;
import com.rpc.transport.socket.util.ObjectWriter;
import com.rpc.utile.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:58
 * @description: Socket方式远程方法调用的消费者（客户端）
 **/
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final ServiceDiscovery serviceDiscovery;

    private final CommonSerializer serializer;

    /**
     * 默认创建
     */
    public SocketClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    /**
     * 添加负载均衡的算法
     * @param loadBalancer
     */
    public SocketClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    /**
     * 添加序列化方式
     * @param serializer
     */
    public SocketClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    /**
     * 添加序列化算法、负载均衡方式
     * @param serializer
     * @param loadBalancer
     */
    public SocketClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
    }


    /**
     * 发送请求
     * @param rpcRequest
     * @return
     * @throws InterruptedException
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {

        // 判断是不是设置了序列化方式
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // 获取服务的连接地址和端口
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

        // 创建一个远程的连接
        try (Socket socket = new Socket()) {

            // 获取输入和输出流
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 写入请求
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);

            // 读取服务端信息
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;

            // 如果响应信息未空抛异常
            if (rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }

            // 如果响应状态码为空，或者响应错误抛异常
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }

            // 响应数据检查
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse;

        }catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }
}
