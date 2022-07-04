package com.rpc.transport;

import com.rpc.serializer.CommonSerializer;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:48
 * @description:
 **/
public interface RpcServer {

    /**
     * 默认的序列化方式是KRYO
     */
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    <T> void publishService(T service, String serviceName);
}
