package com.rpc.client.net;

import com.rpc.client.balance.LoadBalance;
import com.rpc.client.cache.ServerDiscoveryCache;
import com.rpc.client.discovery.ServerDiscovery;
import com.rpc.common.model.Service;
import com.rpc.common.protocol.MessageProtocol;
import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;
import com.rpc.exception.RpcException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端代理工厂：用于创建远程服务代理类
 * 封装请求序列化、请求发送、响应序列化等操作
 */
public class ClientProxyFactory {

    //服务发现
    private ServerDiscovery serverDiscovery;
    //仅支持netty客户端实现，在后期也可以扩展为其他方式实现
    private NetClient netClient;
    //编解码方式集合（也就是序列化，反序列化协议）
    private Map<String, MessageProtocol> supportMessageProtocols;

    private Map<Class<?>, Object> objectCache = new HashMap<>();
    //哪种负均衡策略
    private LoadBalance loadBalance;

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz))
        );
    }


    //这里在Spring在启动上下文的时候，会执行Bean
    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }
            // 1.获得服务信息
            String serviceName = clazz.getName();
            //获取服务列表
            List<Service> services = getServiceList(serviceName);
            //根据负载均衡策略选取一个服务
            Service service = loadBalance.chooseOne(services);
            // 2.构造request对象
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(service.getName());
            request.setMethod(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());
            // 3.协议层编组
            MessageProtocol messageProtocol = supportMessageProtocols.get(service.getProtocol());
            //发送请求并拿到结果
            RpcResponse response = netClient.sendRequest(request, service, messageProtocol);
            // 编组请求
//            byte[] reqData = messageProtocol.marshallingRequest(request);
//            // 4. 调用网络层发送请求
//            byte[] respData = netClient.sendRequest(reqData, service);
//
//            // 5. 解组响应消息
//            RpcResponse response = messageProtocol.unmarshallingResponse(respData);
            if (response == null){
                throw new RpcException("the response is null");
            }
            // 6.结果处理
            if (response.getException() != null) {
                return response.getException();
            }

            return response.getReturnValue();
        }
    }

    /**
     * 根据服务名获取可用的服务地址列表
     * @param serviceName
     * @return
     */
    private List<Service> getServiceList(String serviceName) {
        List<Service> services;
        synchronized (serviceName){
            //先从缓存里拿
            if (ServerDiscoveryCache.isEmpty(serviceName)) {
                //缓存没有再去，zookeeper取出服务信息
                services = serverDiscovery.findServiceListByRegisterCenter(serviceName);
                if (services == null || services.size() == 0) {
                    throw new RpcException("No provider available!");
                }
                //取出来之后，将服务信息放入本地缓存
                ServerDiscoveryCache.put(serviceName, services);
            } else {
                services = ServerDiscoveryCache.get(serviceName);
            }
        }
        return services;
    }


    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }

    public void setServerDiscovery(ServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    public NetClient getNetClient() {
        return netClient;
    }

    public void setNetClient(NetClient netClient) {
        this.netClient = netClient;
    }

    public Map<String, MessageProtocol> getSupportMessageProtocols() {
        return supportMessageProtocols;
    }

    public void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        this.supportMessageProtocols = supportMessageProtocols;
    }

    public Map<Class<?>, Object> getObjectCache() {
        return objectCache;
    }

    public void setObjectCache(Map<Class<?>, Object> objectCache) {
        this.objectCache = objectCache;
    }
}
