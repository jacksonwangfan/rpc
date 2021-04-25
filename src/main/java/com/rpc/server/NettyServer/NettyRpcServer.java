package com.rpc.server.NettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyRpcServer extends RpcServer {

    private static Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private Channel channel;

    public NettyRpcServer(int port, String protocol, RequestInvokeHandler requestInvokeHandler) {
        super(port, protocol, requestInvokeHandler);
    }

    @Override
    public void start() {
        // 配置服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            //配置接受连接线程组，配置读写io的线程组
            b.group(bossGroup, workerGroup)
                    //设置服务器端通道的一个实现
                    .channel(NioServerSocketChannel.class)
                    //设置可连接队列大小为100
                    .option(ChannelOption.SO_BACKLOG, 100)
                    //添加出入站的Handler记录服务日志
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //添加入站处理规则
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ChannelRequestHandler(requestInvokeHandler));
                }
            });
            // 启动服务
            ChannelFuture future = b.bind(port).sync();
            logger.debug("Server started successfully.");
            channel = future.channel();
            // 等待服务通道关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("start netty sever failed,msg:{}", e.getMessage());
        } finally {
            // 释放线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("netty server stop successful");
        }
    }

    @Override
    public void stop() {
        this.channel.close();
    }
}
