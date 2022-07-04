package com.rpc.codec;

import com.rpc.entity.RpcRequest;
import com.rpc.enumeration.PackageType;
import com.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:25
 * @description: 通用的编码拦截器
 **/
public class CommonEncoder extends MessageToByteEncoder {

    /**
     * 魔数和序列化器
     */
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private final CommonSerializer serializer;
    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 进行编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 写入魔数
        out.writeInt(MAGIC_NUMBER);

        // 判断请求消息的类型
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }

        // 写入序列化器的编码
        out.writeInt(serializer.getCode());
        byte[] bytes = serializer.serialize(msg);

        // 写入消息的长度
        out.writeInt(bytes.length);
        // 写入消息
        out.writeBytes(bytes);
    }
}
