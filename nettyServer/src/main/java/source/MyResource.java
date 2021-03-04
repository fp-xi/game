package source;

import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyResource {

    //登录用户数
    private static Map<String, ChannelPromise> currentClient = new ConcurrentHashMap<String, ChannelPromise>();

    public static void login(String user, ChannelPromise channelPromise) {
        currentClient.put(user, channelPromise);
    }

    public static void logout(String user) {
        currentClient.remove(user);
    }
}
