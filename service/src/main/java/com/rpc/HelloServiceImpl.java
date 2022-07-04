package com.rpc;

import com.rpc.annotation.Service;
import com.rpc.api.HelloObject;
import com.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:52
 * @description:
 **/
@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到消息：{}", object.getMessage());
        return "这是Impl1方法";
    }
}
