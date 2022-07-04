package com.rpc;

import com.rpc.annotation.ServiceScan;
import com.rpc.serializer.CommonSerializer;
import com.rpc.transport.RpcServer;
import com.rpc.transport.socket.server.SocketServer;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-06 20:14
 * @description:
 **/
@ServiceScan
public class SocketTestServer {

    public static void main(String[] args) {
        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        server.start();
    }

}
