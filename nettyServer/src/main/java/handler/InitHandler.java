package handler;

import custom.CustomChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import resource.ServerResource;

import java.net.InetSocketAddress;

public class InitHandler extends ChannelInitializer {

    protected void initChannel(Channel channel) throws Exception {
        String clientIp = ((InetSocketAddress)(channel.remoteAddress())).getAddress().getHostAddress();
        System.out.println(clientIp);

        if(ServerResource.currentClient.get(clientIp) == null) {
            CustomChannel customChannel = new CustomChannel();
            customChannel.setChannel(channel);
            if("58.213.224.243".equals(clientIp)) {
                customChannel.setName("fp");
            } else if("58.221.161.26".equals(clientIp)){
                customChannel.setName("ck");
            } else if("58.212.164.77".equals(clientIp)){
                customChannel.setName("Lit");
            } else {
                customChannel.setName("noName");
            }
            ServerResource.login(clientIp, customChannel);

        }
    }
}
