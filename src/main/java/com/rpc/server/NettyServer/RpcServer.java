package com.rpc.server.NettyServer;

import com.rpc.server.handlers.RequestProcesser;

/**
 * Rpc服务端抽象类
 */
public abstract class RpcServer {

    /**
     * 服务端口
     */
    protected int port;
    /**
     * 服务协议
     */
    protected String protocol;
    /**
     * 请求处理者
     */
    protected RequestProcesser requestProcesser;

    public RpcServer(int port, String protocol, RequestProcesser requestProcesser) {
        this.port = port;
        this.protocol = protocol;
        this.requestProcesser = requestProcesser;
    }

    /**
     * 开启服务
     */
    public abstract void start();

    /**
     * 关闭服务
     */
    public abstract void stop();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public RequestProcesser getRequestProcesser() {
        return requestProcesser;
    }

    public void setRequestProcesser(RequestProcesser requestProcesser) {
        this.requestProcesser = requestProcesser;
    }
}
