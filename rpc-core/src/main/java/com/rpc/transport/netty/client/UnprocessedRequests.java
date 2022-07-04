package com.rpc.transport.netty.client;

import com.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 23:18
 * @description: 未处理的请求
 **/
public class UnprocessedRequests {

    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    /**
     * 添加未处理的请求
     * @param requestId 请求的id
     * @param future 返回结果
     */
    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseFutures.put(requestId, future);
    }


    /**
     * 删除未处理的请求
     * @param requestId
     */
    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    /**
     * 完成请求将其清除
     * @param rpcResponse
     */
    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprocessedResponseFutures.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
