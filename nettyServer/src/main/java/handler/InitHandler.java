package handler;

import custom.CustomChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import resource.ServerResource;
import util.JedisUtil;

import java.net.InetSocketAddress;

public class InitHandler extends ChannelInitializer {

    protected void initChannel(Channel channel) throws Exception {
        String clientIp = ((InetSocketAddress)(channel.remoteAddress())).getAddress().getHostAddress();
        System.out.println(clientIp);

        if(ServerResource.currentClient.get(clientIp) == null) {
            Jedis jedis = JedisUtil.getJedis();
            String userName = jedis.get(clientIp);
            if(StringUtils.isNotBlank(userName) && !"null".equals(userName)) {
                CustomChannel customChannel = new CustomChannel();
                customChannel.setChannel(channel);
                customChannel.setName(userName);
                ServerResource.login(clientIp, customChannel);

            }
            JedisUtil.close(jedis);
        }
    }
}
