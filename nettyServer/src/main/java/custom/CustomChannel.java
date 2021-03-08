package custom;

import io.netty.channel.Channel;

import java.util.List;

public class CustomChannel {

    private Channel channel;
    private String name;
    private List<String> pokers;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPokers() {
        return pokers;
    }

    public void setPokers(List<String> pokers) {
        this.pokers = pokers;
    }
}
