package handler;

import custom.CustomChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import resource.ServerResource;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        System.out.println("from client:" + message);
        CustomChannel customChannel = ServerResource.getCustom(ctx.channel());
        String ip = ((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress().getHostAddress();
        //判断用户是否注册
        if(customChannel == null) {
            ctx.writeAndFlush("You have not registered,please contact system administrator!",ctx.channel().voidPromise());
        }else {
            if("exit".equals(message.toLowerCase())){
                ServerResource.loop.remove(ip);
            } else if("join".equals(message.toLowerCase())){
                ServerResource.loop.add(ip);
            } else if("list".equals(message.toLowerCase())) {
                StringBuffer sb = new StringBuffer();
                sb.append("current online:").append("\r\n");
                ServerResource.currentClient.forEach((key,value) -> {
                    sb.append(key).append(":").append(value.getName()).append("\r\n");
                });
                serverMessage(ctx,sb.toString(),customChannel);
            } else if(message.toLowerCase().startsWith("kick:")){
                String[] s = message.split(":");
                if(s.length == 2) {
                    serverMessageToAll(ctx, ServerResource.currentClient.get(s[1]).getName() + " has been kicked");
                    ServerResource.logout(s[1]);
                }
            }else if(message.startsWith(":") || message.startsWith("：")){
                String me = message.substring(1);
                sendMessageToAll(ctx, me, customChannel);
            } else {
                if("running".equals(ServerResource.status)) {
                    if("l".equals(message.toLowerCase())) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("landlord is ").append(ServerResource.currentClient.get(ServerResource.host).getName()).append("\r\n");
                        ServerResource.currentClient.forEach((key,value) -> {
                            sb.append(value.getName()).append(" left ").append(value.getPokers().size()).append(" cards,");
                        });
                        sb.append("\r\n");
                        sb.append("current pokers:");
                        customChannel.getPokers().stream().sorted().forEach(item ->sb.append(item).append(","));
                        serverMessage(ctx,sb.substring(0,sb.length()-1), customChannel);
                    } else if("p".equals(message.toLowerCase()) && ip.equals(ServerResource.current)){
                        ServerResource.current = ServerResource.getNext(ip);
                        serverMessageToAll(ctx,customChannel.getName()+ " pass,next " + ServerResource.currentClient.get(ServerResource.current).getName()+"\r\n");
                    } else {
                        if(ip.equals(ServerResource.current)) {
                            String result = ServerResource.push(message, ip);
                            if("win".equals(result)) {
                                StringBuffer s = new StringBuffer();
                                Arrays.asList(message.split(",")).stream().sorted().forEach(item -> s.append(item).append(","));
                                serverMessageToAll(ctx, customChannel.getName()+"------>"+ s.toString());
                                StringBuffer finalS = new StringBuffer();
                                finalS.append("\r\n");
                                ServerResource.currentClient.forEach((key, value) -> {
                                    finalS.append(value.getName()).append(" left :");
                                    value.getPokers().stream().sorted().forEach(item -> finalS.append(item).append(","));
                                    finalS.append("\r\n");
                                });
                                serverMessageToAll(ctx, finalS.toString());
                                serverMessageToAll(ctx,customChannel.getName() + " win!");
                                ServerResource.status = "undo";
                            } else if("success".equals(result)){
                                StringBuffer s = new StringBuffer();
                                Arrays.asList(message.split(",")).stream().sorted().forEach(item -> s.append(item).append(","));
                                serverMessageToAll(ctx, customChannel.getName()+"------>"+ s.toString());
                                ServerResource.current = ServerResource.getNext(ip);
                                serverMessageToAll(ctx, "next "+ ServerResource.currentClient.get(ServerResource.current).getName());
                            } else {
                                serverMessage(ctx, "error,retry!",customChannel);
                            }
                        } else {
                            serverMessage(ctx, "not your turn", customChannel);
                        }

                    }
                }else if("qiang".equals(ServerResource.status)){
                    if("l".equals(message.toLowerCase())) {
                        StringBuffer sb = new StringBuffer();
                        customChannel.getPokers().stream().sorted().forEach(item ->sb.append(item).append(","));
                        serverMessage(ctx,"current pokers:"+sb.substring(0,sb.length()-1), customChannel);
                    }else if("c".equals(message.toLowerCase()) && ip.equals(ServerResource.current)) {
                        ServerResource.times = 0;
                        ServerResource.status = "running";
                        ServerResource.host = ip;
                        StringBuffer s = new StringBuffer();
                        ServerResource.leftPokers.forEach(item -> s.append(item).append(","));
                        serverMessageToAll(ctx, "landlord is " + customChannel.getName());
                        serverMessageToAll(ctx, "left cards: " + s.toString());
                        ServerResource.pull(ip);
                        serverMessageToAll(ctx,"current "+ customChannel.getName());
                    } else if("w".equals(message.toLowerCase()) && ip.equals(ServerResource.current)) {
                        ServerResource.current = ServerResource.getNext(ip);
                        ServerResource.times++;
                        if(ServerResource.times == 3){
                            serverMessageToAll(ctx, "no one claim for landlord,shuffle cards");
                            ServerResource.shuffle();
                            ServerResource.deal();
                            ServerResource.times = 0;
                            serverMessageToAll(ctx, customChannel.getName() + " claim for landlord");
                        }else {
                            serverMessageToAll(ctx, customChannel.getName() + " give up," +
                                    " turn to " + ServerResource.currentClient.get(ServerResource.current).getName());
                        }
                    }
                }else {
                    if("start".equals(message.toLowerCase())) {
                        if(ServerResource.currentClient.size() == 3) {
                            ServerResource.refresh();
                            ServerResource.status = "qiang";
                            ServerResource.current = ip;
                            ServerResource.deal();
                            serverMessageToAll(ctx,customChannel.getName() +" start the game");
                            serverMessageToAll(ctx, customChannel.getName() + " claim for landlord");

                        } else {
                            serverMessageToAll(ctx,"less than 3 people!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // cause.printStackTrace();
        String clientIp = ((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress().getHostAddress();
        CustomChannel customChannel = ServerResource.getCustom(ctx.channel());
        System.out.println(customChannel.getName() + " has logged out");
        serverMessageToAll(ctx,customChannel.getName()+" has logged out");
        //清空手牌
        //todo
        ServerResource.logout(clientIp);
        ServerResource.status = "undo";
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //监听状态
    }

    private void sendMessage(ChannelHandlerContext ctx, String message, CustomChannel customChannel) {
        ctx.writeAndFlush(customChannel.getName()+":"+message,customChannel.getChannel().voidPromise());
    }

    private void sendMessageToAll(ChannelHandlerContext ctx, String message, CustomChannel customChannel) {
        ServerResource.currentClient.forEach((key,value) -> {
            value.getChannel().writeAndFlush(customChannel.getName()+":"+message);
        });
    }

    private void serverMessage(ChannelHandlerContext ctx, String message, CustomChannel customChannel){
        ctx.writeAndFlush(message, customChannel.getChannel().voidPromise());
    }

    private void serverMessageToAll(ChannelHandlerContext ctx, String message){
        ServerResource.currentClient.forEach((key,value) -> {
            value.getChannel().writeAndFlush(message);
        });
    }
}
