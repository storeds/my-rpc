package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:24
 * @description: 字节流中标识序列化和反序列化器
 **/
@AllArgsConstructor
@Getter
public enum SerializerCode {

    KRYO(0),
    JSON(1),
    HESSIAN(2),
    PROTOBUF(3);

    private final int code;

}
