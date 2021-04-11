package com.rpc.client.net;

import com.rpc.common.model.Service;
import com.rpc.common.protocol.MessageProtocol;
import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;

/**
 *
 * 网络请求客户端，定义请求规范
 *
 */
public interface NetClient {

    byte[] sendRequest(byte[] data, Service service) throws InterruptedException;

    RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol);
}
