package com.rpc.codec;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.PackageType;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 22:00
 * @description: 通用的解码拦截器
 **/
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);
    /**
     * 定义的魔数
     */
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 获取魔数头部
        int magic = in.readInt();

        // 不被识别的协议包
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        // 获取包的类型
        int packageCode = in.readInt();
        Class<?> packageClass;
        // 如果是请求数据包，响应数据包，无法识别的包
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        // 获取序列号
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        // 获取长度
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // 将序列化对象添加到下一步处理
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }
}
