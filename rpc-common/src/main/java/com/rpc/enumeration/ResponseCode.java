package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:19
 * @description:
 **/
@AllArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200, "调用方法成功"),
    FAIL(500, "调用方法失败"),
    METHOD_NOT_FOUND(500, "未找到指定方法"),
    CLASS_NOT_FOUND(500, "未找到指定类");

    private final int code;
    private final String message;
}
