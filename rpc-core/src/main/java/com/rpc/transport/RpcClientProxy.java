package com.rpc.transport;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.transport.netty.client.NettyClient;
import com.rpc.transport.socket.client.SocketClient;
import com.rpc.utile.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:12
 * @description: RPC客户端动态代理
 **/
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }


    /**
     * 获取代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());

        // 生成rpc请求的基本信息
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false);

        RpcResponse rpcResponse = null;

        // 如果客户端是自己的客户端，那么发送请求
        if (client instanceof NettyClient) {
            try {
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            }catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }

        // 如果client是socketClient那么发送请求
        if (client instanceof SocketClient) {
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }

        // 再次检查响应信息
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
