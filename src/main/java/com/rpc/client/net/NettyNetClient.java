package com.rpc.client.net;


import com.rpc.client.net.handler.SendHandler;
import com.rpc.client.net.handler.SendHandlerV2;
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
    public static Map<String, SendHandlerV2> connectedServerNodes = new ConcurrentHashMap<>();

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol) {

        String address = service.getAddress();
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendHandlerV2 handler = connectedServerNodes.get(address);
                logger.info("使用现有的连接");
                return handler.sendRequest(rpcRequest);
            }

            String[] addrInfo = address.split(":");
            final String serverAddress = addrInfo[0];
            final String serverPort = addrInfo[1];
            final SendHandlerV2 handler = new SendHandlerV2(messageProtocol, address);
            threadPool.submit(() -> {
                        // 配置客户端
                        Bootstrap b = new Bootstrap();
                        b.group(loopGroup).channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        ChannelPipeline pipeline = socketChannel.pipeline();
                                        pipeline
//                                                .addLast(new FixedLengthFrameDecoder(20))
                                                .addLast(handler);
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
            return handler.sendRequest(rpcRequest);
        }
    }
}
