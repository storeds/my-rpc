package com.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.loadbalancer.LoadBalancer;
import com.rpc.loadbalancer.RandomLoadBalancer;
import com.rpc.utile.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:33
 * @description: 服务发现
 **/
public class NacosServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);
    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        // 默认使用随机算法进行注册
        if(loadBalancer == null) this.loadBalancer = new RandomLoadBalancer();
        else this.loadBalancer = loadBalancer;
    }


    @Override
    public InetSocketAddress lookupService(String serviceName){
        try {
            // 获取所有的实例
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            // 没有实例将会抛出异常
            if(instances.size() == 0) {
                logger.error("找不到对应的服务: " + serviceName);
                throw new RpcException(RpcError.SERVICE_NOT_FOUND);
            }
            // 选择负载均衡的算法，获取服务
            Instance instance = loadBalancer.select(instances);

            // 返回服务
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }
}
