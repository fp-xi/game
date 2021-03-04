package handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当channel就绪后。
        System.out.println("client channel is ready!");
        //ctx.writeAndFlush("started");//阻塞知道发送完毕
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        System.out.println(message);
    }


    public String call(String message, Channel channel) throws Exception {
        channel.writeAndFlush(message);
        return message;
    }
}
