package com.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:19
 * @description: 轮询算法
 **/
public class RoundRobinLoadBalancer implements LoadBalancer {
    private int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        if(index >= instances.size()) {
            index %= instances.size();
        }
        return instances.get(index++);
    }
}
