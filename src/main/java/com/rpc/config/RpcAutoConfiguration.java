package com.rpc.config;

import com.rpc.annotation.MessageProtocolAno;
import com.rpc.client.balance.LoadBalance;
import com.rpc.client.discovery.ZookeeperServerDiscovery;
import com.rpc.client.net.ClientProxyFactory;
import com.rpc.client.net.NettyNetClient;
import com.rpc.common.protocol.MessageProtocol;
import com.rpc.exception.RpcException;
import com.rpc.server.NettyServer.NettyRpcServer;
import com.rpc.server.handlers.RequestProcesser;
import com.rpc.server.NettyServer.RpcServer;
import com.rpc.server.register.DefaultRpcProcessor;
import com.rpc.server.register.ServerRegister;
import com.rpc.server.register.ZookeeperServerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 注入需要的bean
 *
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {

    @Bean
    public RpcConfig rpcConfig() {
        return new RpcConfig();
    }

    /**
     * 实例化服务注册者
     * @param rpcConfig
     * @return
     */
    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig) {
        return new ZookeeperServerRegister(
                rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(),
                rpcConfig.getProtocol(),
                rpcConfig.getWeight());
    }

    /**
     * 实例化请求处理器
     * @param serverRegister
     * @param rpcConfig
     * @return
     */
    @Bean
    public RequestProcesser requestHandler(@Autowired ServerRegister serverRegister,
                                           @Autowired RpcConfig rpcConfig) {
        return new RequestProcesser(getMessageProtocol(rpcConfig.getProtocol()), serverRegister);
    }

    /**
     * 实例化RPC Server,每个服务都有一个RCP Server
     * @param requestProcesser
     * @param rpcConfig
     * @return
     */
    @Bean
    public RpcServer rpcServer(@Autowired RequestProcesser requestProcesser,
                               @Autowired RpcConfig rpcConfig) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestProcesser);
    }


    /**
     * 配置客户端代理的实现
     * @return
     */
    @Bean
    public ClientProxyFactory proxyFactory(@Autowired RpcConfig rpcConfig) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        // 设置服务发现者
        clientProxyFactory.setServerDiscovery(new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress()));
        // SPI机制设置支持的协议
        Map<String, MessageProtocol> supportMessageProtocols = buildSupportMessageProtocols();
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocols);
        // 设置负载均衡算法
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        clientProxyFactory.setLoadBalance(loadBalance);
        // 设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    /**
     * 配置序列化和反序列机制的实现
     * @param name
     * @return
     */
    private MessageProtocol getMessageProtocol(String name) {
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        Iterator<MessageProtocol> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MessageProtocol messageProtocol = iterator.next();
            MessageProtocolAno ano = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            if (name.equals(ano.value())) {
                return messageProtocol;
            }
        }
        throw new RpcException("invalid message protocol config!");
    }


    private Map<String, MessageProtocol> buildSupportMessageProtocols() {
        Map<String, MessageProtocol> supportMessageProtocols = new HashMap<>();
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        Iterator<MessageProtocol> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MessageProtocol messageProtocol = iterator.next();
            MessageProtocolAno ano = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            supportMessageProtocols.put(ano.value(), messageProtocol);
        }
        return supportMessageProtocols;
    }

    /**
     * 使用spi匹配符合配置的负载均衡算法
     *
     * @param name
     * @return
     */
    private LoadBalance getLoadBalance(String name) {
        ServiceLoader<com.rpc.client.balance.LoadBalance> loader = ServiceLoader.load(com.rpc.client.balance.LoadBalance.class);
        Iterator<com.rpc.client.balance.LoadBalance> iterator = loader.iterator();
        while (iterator.hasNext()) {
            com.rpc.client.balance.LoadBalance loadBalance = iterator.next();
            com.rpc.annotation.LoadBalance ano = loadBalance.getClass().getAnnotation(com.rpc.annotation.LoadBalance.class);
            Assert.notNull(ano, "load balance name can not be empty!");
            if (name.equals(ano.value())) {
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config");
    }

    /**
     * 实例化RPC框架中的注解处理器，扫描我们的自定注解，作出相应的处理
     * @param clientProxyFactory
     * @param serverRegister
     * @param rpcServer
     * @return
     */
    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer);
    }


}
