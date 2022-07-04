package com.rpc.exception;

import com.rpc.enumeration.RpcError;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:25
 * @description: RPC调用异常
 **/
public class RpcException extends RuntimeException {

    public RpcException(RpcError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError error) {
        super(error.getMessage());
    }

}
