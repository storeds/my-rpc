package com.rpc.provider;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:39
 * @description: 保存和提供服务实例对象
 **/
public interface ServiceProvider {

    <T> void addServiceProvider(T service, String serviceName);

    Object getServiceProvider(String serviceName);

}
