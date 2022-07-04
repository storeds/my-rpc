package com.rpc.transport.socket.util;

import com.rpc.entity.RpcRequest;
import com.rpc.entity.RpcResponse;
import com.rpc.enumeration.PackageType;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:11
 * @description:
 **/
public class ObjectReader {

    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static Object readObject(InputStream in) throws IOException {
        byte[] numberBytes = new byte[4];

        // 获取魔数
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);
        // 检查魔数的合法性
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        // 获取检测是请求还是响应
        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;
        // 如果是请求
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        // 获取序列化算法
        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        // 获取对象并解码返回
        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);
    }

    public static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF)<<24)
                |((src[1] & 0xFF)<<16)
                |((src[2] & 0xFF)<<8)
                |(src[3] & 0xFF);
        return value;
    }

}
