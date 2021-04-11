package com.rpc.common.protocol;

import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;

/**
 *消息协议:定义请求编码、请求解码、响应编码、响应解码规范
 */
public interface MessageProtocol {
    /**
     * 请求编码
     * @param request 请求信息
     * @return 请求字节数组
     * @throws Exception
     */
    byte[] marshallingRequest(RpcRequest request) throws Exception;

    /**
     * 请求解码
     * @param data
     * @return
     * @throws Exception
     */
    RpcRequest unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 响应编码
     * @param response
     * @return
     */
    byte[] marshallingResponse(RpcResponse response) throws Exception;

    /**
     * 响应解码
     * @param data
     * @return
     * @throws Exception
     */
    RpcResponse unmarshallingResponse(byte[] data) throws Exception;
}
