package handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class TestHandler extends ChannelInitializer {

    protected void initChannel(Channel channel) throws Exception {
        System.out.println(channel);
    }
}
