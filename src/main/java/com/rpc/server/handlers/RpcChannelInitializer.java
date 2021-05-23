package com.rpc.server.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class RpcChannelInitializer extends ChannelInitializer {

    private RequestProcesser requestProcesser;

    public RpcChannelInitializer(RequestProcesser requestProcesser){
        this.requestProcesser = requestProcesser;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new ChannelRequestHandler(requestProcesser));
    }
}
