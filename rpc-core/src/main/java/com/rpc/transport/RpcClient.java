package com.rpc.transport;

import com.rpc.entity.RpcRequest;
import com.rpc.serializer.CommonSerializer;

import java.io.IOException;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:11
 * @description: 客户端通用接口
 **/
public interface RpcClient {

    /** 默认的序列化方法 **/
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    /** 发送请求 **/
    Object sendRequest(RpcRequest rpcRequest) throws InterruptedException, IOException;

}
