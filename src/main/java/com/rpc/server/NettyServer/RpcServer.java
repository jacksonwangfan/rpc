package com.rpc.server.NettyServer;

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
    protected RequestInvokeHandler requestInvokeHandler;

    public RpcServer(int port, String protocol, RequestInvokeHandler requestInvokeHandler) {
        this.port = port;
        this.protocol = protocol;
        this.requestInvokeHandler = requestInvokeHandler;
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

    public RequestInvokeHandler getRequestInvokeHandler() {
        return requestInvokeHandler;
    }

    public void setRequestInvokeHandler(RequestInvokeHandler requestInvokeHandler) {
        this.requestInvokeHandler = requestInvokeHandler;
    }
}
