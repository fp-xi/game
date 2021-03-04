package handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.codec.digest.DigestUtils;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        System.out.println("from client:" + message);
        JSONObject json = JSONObject.parseObject(message);
        String source = json.getString("source");

        String md5 = DigestUtils.md5Hex(source);
        //解析成JSON
        json.put("md5Hex",md5);
        ctx.writeAndFlush(json.toString());//write bytes to socket,and flush(clear) the buffer cache.
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当channel就绪后。
        System.out.println("client channel is ready!");
        while (true) {
            Thread.sleep(1000);
            JSONObject json = new JSONObject();
            json.put("id",2);
            json.put("source","start");
            ctx.writeAndFlush(json.toString());//阻塞知道发送完毕
        }

    }
}
