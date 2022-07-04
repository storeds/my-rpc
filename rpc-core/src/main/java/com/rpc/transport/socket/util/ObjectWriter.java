package com.rpc.transport.socket.util;

import com.rpc.entity.RpcRequest;
import com.rpc.enumeration.PackageType;
import com.rpc.serializer.CommonSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:06
 * @description:
 **/
public class ObjectWriter {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 写入对象
     * @param outputStream
     * @param object  对象
     * @param serializer  序列化方式
     * @throws IOException
     */
    public static void  writeObject(OutputStream outputStream, Object object, CommonSerializer serializer) throws IOException {
        // 写入魔数
        outputStream.write(intToBytes(MAGIC_NUMBER));

        // 判断是请求还是响应
        if (object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageType.REQUEST_PACK.getCode()));
        } else {
            outputStream.write(intToBytes(PackageType.RESPONSE_PACK.getCode()));
        }

        // 写入序列化算法类型
        outputStream.write(intToBytes(serializer.getCode()));

        // 写入对象的长度和对象
        byte[] bytes = serializer.serialize(object);
        outputStream.write(intToBytes(bytes.length));
        outputStream.write(bytes);

        // 刷新发送
        outputStream.flush();
    }

    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

}
