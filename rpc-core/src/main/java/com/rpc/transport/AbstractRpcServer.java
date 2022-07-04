package com.rpc.transport;

import com.rpc.annotation.Service;
import com.rpc.annotation.ServiceScan;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.provider.ServiceProvider;
import com.rpc.registry.ServiceRegistry;
import com.rpc.utile.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:48
 * @description:
 **/
public class AbstractRpcServer  implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /** ip地址和端口号 **/
    protected String host;
    protected int port;

    /** 服务注册接口 保存和提供服务实例对象  **/
    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;


    /**
     * 启动方法
     */
    @Override
    public void start() {

    }

    /**
     * 公共服务方法
     * @param service
     * @param serviceName
     * @param <T>
     */
    @Override
    public <T> void publishService(T service, String serviceName) {
        // 保存服务实例对象，注册和添加服务
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

    /**
     * 扫描服务
     */
    public void scanServices() {
        // 获取类的信息
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;

        // 获取配置类，判断有没有携带@ServiceScan的注解
        try {
            startClass = Class.forName(mainClassName);
            // 如果这个类缺少服务扫描注解则报错
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        }catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }

        // 获取包的位置
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }

        // 通过之前写的反射工具获取所有的类
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);

        // 判断是否携带@Sevice注解，有提供远程服务
        for(Class<?> clazz : classSet) {
            // 携带了@Sevice注解
            if(clazz.isAnnotationPresent(Service.class)) {
                // 获取@Sevice注解中的值
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    // 反射创建对象
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                // 如果值为空，设置为默认的处理，最终将其添加到公共服务方法中，提供调用服务
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }

    }
}
