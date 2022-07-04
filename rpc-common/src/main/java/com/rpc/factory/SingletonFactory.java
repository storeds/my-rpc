package com.rpc.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:27
 * @description: 单例工厂
 **/
public class SingletonFactory {

    private static Map<Class, Object> objectMap = new ConcurrentHashMap<>();

    private SingletonFactory() {}

    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);

        synchronized (clazz) {
            if(instance == null) {
                try {
                    instance = clazz.newInstance();
                    objectMap.put(clazz, instance);
                }catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return clazz.cast(instance);
    }

}
