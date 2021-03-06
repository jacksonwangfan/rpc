package com.rpc.server.handlers;

import com.rpc.common.protocol.MessageProtocol;
import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;
import com.rpc.common.constants.RpcStatusEnum;
import com.rpc.server.register.ServerRegister;
import com.rpc.server.register.ServiceObject;

import java.lang.reflect.Method;

/**
 *
 * 请求处理者，提供解组请求、编组响应等操作
 */
public class RequestProcesser {

    private MessageProtocol protocol;

    private ServerRegister serverRegister;

    public RequestProcesser(MessageProtocol protocol, ServerRegister serverRegister) {
        this.protocol = protocol;
        this.serverRegister = serverRegister;
    }


    public byte[] handleRequest(byte[] data) throws Exception {
        // 1.解组消息
        RpcRequest req = this.protocol.unmarshallingRequest(data);
        // 2.查找服务对应,校验对用存在的服务
        ServiceObject so = serverRegister.getServiceObject(req.getServiceName());
        RpcResponse response = null;
        if (so == null){
            response = new RpcResponse(RpcStatusEnum.NOT_FOUND);
        }else {
            try {
                // 3.反射调用对应的方法过程
                Method method = so.getClazz().getMethod(req.getMethod(), req.getParameterTypes());
                Object returnValue = method.invoke(so.getObj(), req.getParameters());
                response = new RpcResponse(RpcStatusEnum.SUCCESS);
                response.setReturnValue(returnValue);
            }catch (Exception e){
                response = new RpcResponse(RpcStatusEnum.ERROR);
                response.setException(e);
            }
        }
        // 编组响应消息
        response.setRequestId(req.getRequestId());
        return this.protocol.marshallingResponse(response);
    }


    public MessageProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    public ServerRegister getServerRegister() {
        return serverRegister;
    }

    public void setServerRegister(ServerRegister serverRegister) {
        this.serverRegister = serverRegister;
    }
}
