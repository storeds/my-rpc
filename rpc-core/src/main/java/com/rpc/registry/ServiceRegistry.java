package com.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:31
 * @description: 服务注册接口
 **/
public interface ServiceRegistry {

    /**
     * 将一个服务注册进注册表
     * @param serviceName 服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

}
