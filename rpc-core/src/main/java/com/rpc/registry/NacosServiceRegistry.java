package com.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.utile.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:32
 * @description: Nacos服务注册中心
 **/
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    /**
     * 进行服务注册
     * @param serviceName 服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }
}
