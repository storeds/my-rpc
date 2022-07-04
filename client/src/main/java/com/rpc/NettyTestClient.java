package com.rpc;

import com.rpc.api.ByeService;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcClient;
import com.rpc.transport.RpcClientProxy;
import com.rpc.transport.netty.client.NettyClient;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:48
 * @description:
 **/
public class NettyTestClient {

    public static void main(String[] args) {
        // 初始化一个rpc的client
        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
        // 创建一个代理
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        //        // 调用代理的方法
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        // 向服务端发送消息
        HelloObject object = new HelloObject(12, "This is a message");
        // 服务端返回结果
        String res = helloService.hello(object);
        System.out.println(res);
        ByeService byeService = rpcClientProxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Netty"));
    }

}
