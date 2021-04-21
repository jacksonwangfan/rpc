package com.rpc.client.net;


import com.rpc.client.net.handler.SendHandler;
import com.rpc.common.model.Service;
import com.rpc.common.protocol.MessageProtocol;
import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;


public class NettyNetClient implements NetClient {

    private static Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    private static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder()
            .setNameFormat("rpcClient-%d")
            .build());

    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    public static Map<String, SendHandler> connectedServerNodes = new ConcurrentHashMap<>();

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol) {
        String address = service.getAddress();
        //这里锁加的有待优化
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendHandler handler = connectedServerNodes.get(address);
                logger.info("使用现有的连接");
                return handler.sendRequest(rpcRequest);
            }
            final SendHandler handler = getSendHandler(messageProtocol, address);
            return handler.sendRequest(rpcRequest);
        }
    }

    private SendHandler getSendHandler(MessageProtocol messageProtocol, String address) {
        String[] addrInfo = address.split(":");
        final String serverAddress = addrInfo[0];
        final String serverPort = addrInfo[1];
        final SendHandler handler = new SendHandler(messageProtocol, address);
        //将配置客户端的工作提交到线程池
        threadPool.submit(() -> {
                    // 配置客户端
                    Bootstrap b = new Bootstrap();
                    b.group(loopGroup).channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    ChannelPipeline pipeline = socketChannel.pipeline();
                                    pipeline.addLast(handler);
                                }
                            });
                    // 启用客户端连接
                    ChannelFuture channelFuture = b.connect(serverAddress, Integer.parseInt(serverPort));
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            connectedServerNodes.put(address, handler);
                        }
                    });
                }
        );
        logger.info("使用新的连接。。。");
        return handler;
    }
}
