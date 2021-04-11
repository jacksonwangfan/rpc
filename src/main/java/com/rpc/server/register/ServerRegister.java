package com.rpc.server.register;

/**
 * 服务注册器，定义服务注册规范
 */
public interface ServerRegister {

    void register(ServiceObject so)throws Exception;

    ServiceObject getServiceObject(String name)throws Exception;
}
