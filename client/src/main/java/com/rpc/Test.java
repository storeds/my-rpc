package com.rpc;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.rpc.utile.NacosUtil;

import java.util.List;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-06 19:56
 * @description:
 **/
public class Test {

    public static void main(String[] args) throws NacosException {

        List<Instance> instances = NacosUtil.getAllInstance("com.rpc.api.HelloService");
        System.out.println(instances);
    }

}
