package com.rpc.client.net.handler;

import com.rpc.client.net.NettyNetClient;
import com.rpc.client.net.RpcFuture;
import com.rpc.common.model.RpcRequest;
import com.rpc.common.model.RpcResponse;
import com.rpc.common.protocol.MessageProtocol;
import com.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 处理入站事件
 */
public class SendHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendHandler.class);

    /**
     * 等待通道建立最大时间
     */
    static final int CHANNEL_WAIT_TIME = 10;
    /**
     * 等待响应最大时间
     */
    static final int RESPONSE_WAIT_TIME = 8;

    /**
     * Netty中Channel通道
     */
    private volatile Channel channel;

    private String remoteAddress;

    /**
     * 返回结果存储
     */
    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    /**
     * 序列化协议
     */
    private MessageProtocol messageProtocol;

    /**
     * 使用CountDownLanch，实现读取消息时线程间通信
     */
    private CountDownLatch latch = new CountDownLatch(1);

    public SendHandler(MessageProtocol messageProtocol, String remoteAddress) {
        this.messageProtocol = messageProtocol;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Channel注册事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        latch.countDown();
    }

    /**
     * 有新连接接入
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Connect to server successfully:{}", ctx);
    }

    /**
     * Channel数据入站，有数据传入
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client reads message:{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] resp = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(resp);
        // 手动回收
        ReferenceCountUtil.release(byteBuf);
        RpcResponse response = messageProtocol.unmarshallingResponse(resp);
        RpcFuture<RpcResponse> future = requestMap.get(response.getRequestId());
        future.setResponse(response);
    }

    /**
     * Channel异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Exception occurred:{}", cause.getMessage());
        ctx.close();
    }

    /**
     * Channel数据读取完成
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 丢失连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("channel inactive with remoteAddress:[{}]",remoteAddress);
        NettyNetClient.connectedServerNodes.remove(remoteAddress);
    }

    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response;
        //使用Future接收返回结果
        RpcFuture<RpcResponse> future = new RpcFuture<>();
        //请求-响应集合
        requestMap.put(request.getRequestId(), future);
        try {
            //序列化
            byte[] data = messageProtocol.marshallingRequest(request);
            //申请内存
            ByteBuf reqBuf = Unpooled.buffer(data.length);
            //写入
            reqBuf.writeBytes(data);
            //等待 Channel establish
            if (latch.await(CHANNEL_WAIT_TIME,TimeUnit.SECONDS)){
                //写入数据
                channel.writeAndFlush(reqBuf);
                // 等待响应
                response = future.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
            }else {
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response;
    }


}
