package com.rpc.test.Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class NettyServer {

    public static final int PORT = 8888;
    public static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    public static final EventLoopGroup workGroup = new NioEventLoopGroup(4);
    public static final ServerBootstrap serverBootstrap = new ServerBootstrap();


    public static void main(String[] args) throws InterruptedException {
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpServerCodec())
                                .addLast();
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.WRITE_SPIN_COUNT, 19)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 60)
                .bind(PORT).sync();

    }
}
