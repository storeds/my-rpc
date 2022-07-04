package com.rpc;

import com.rpc.annotation.ServiceScan;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcServer;
import com.rpc.transport.netty.server.NettyServer;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:53
 * @description:
 **/
@ServiceScan
public class NettyTestServer {

    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();
    }

}
