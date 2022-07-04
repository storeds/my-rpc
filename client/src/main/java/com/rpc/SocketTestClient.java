package com.rpc;

import com.rpc.api.ByeService;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcClientProxy;
import com.rpc.transport.socket.client.SocketClient;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-06 20:12
 * @description:
 **/
public class SocketTestClient {

    public static void main(String[] args) {
        SocketClient client = new SocketClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");

        String res = helloService.hello(object);
        System.out.println(res);
        ByeService byeService = proxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Netty"));
    }

}
