package com.rpc.provider;

import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:39
 * @description: 默认的服务注册表，保存服务端本地服务
 **/
public class ServiceProviderImpl  implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /** 服务的注册map 注册的服务 **/
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 添加服务提供者
     * @param service
     * @param serviceName
     * @param <T>
     */
    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        // 如果包含这个服务直接返回空
        if (registeredService.contains(serviceName)) return;

        // 否则添加这个服务
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        logger.info("向接口: {} 注册服务: {}", service.getClass().getInterfaces(), serviceName);
    }

    /**
     * 获取服务提供者
     * @param serviceName
     * @return
     */
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);

        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
