package com.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:19
 * @description:
 **/
public interface LoadBalancer {

    Instance select(List<Instance> instances);

}
