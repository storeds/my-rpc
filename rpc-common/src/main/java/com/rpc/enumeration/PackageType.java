package com.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:21
 * @description:
 **/
@AllArgsConstructor
@Getter
public enum PackageType {

    /**
     * 请求包是0，响应包是1
     */
    REQUEST_PACK(0),
    RESPONSE_PACK(1);
    private final int code;
}
