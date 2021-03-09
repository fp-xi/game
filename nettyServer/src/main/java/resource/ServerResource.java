package resource;

import custom.CustomChannel;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerResource {

    public static String host;
    public static String status = "undo";
    public static int times = 0;
    public static String current;
    public static String lastSend;
    public static List<String> lastCards = new ArrayList<>();

    public static List<String> loop = new ArrayList<>();

    //登录用户数
    public static Map<String, CustomChannel> currentClient = new ConcurrentHashMap<String, CustomChannel>();

    public static void login(String ip, CustomChannel customChannel) {
        currentClient.put(ip, customChannel);
        List<String> temp = copyList(loop);
        temp.add(ip);
        loop = temp;
    }

    public static void logout(String ip) {
        currentClient.remove(ip);
        List<String> temp = copyList(loop);
        temp.remove(ip);
        loop = temp;

    }

    public static CustomChannel getCustom(Channel channel) {
        String clientIp = ((InetSocketAddress)(channel.remoteAddress())).getAddress().getHostAddress();
        return currentClient.get(clientIp);
    }

    public static List<String> leftPokers = new ArrayList<>();

    public static void init(){
        leftPokers.clear();
        for(int i=0;i<4;i++) {
            leftPokers.add("A");
            leftPokers.add("2");
            leftPokers.add("3");
            leftPokers.add("4");
            leftPokers.add("5");
            leftPokers.add("6");
            leftPokers.add("7");
            leftPokers.add("8");
            leftPokers.add("9");
            leftPokers.add("10");
            leftPokers.add("J");
            leftPokers.add("Q");
            leftPokers.add("K");
        }
        leftPokers.add("joker");
        leftPokers.add("Joker");
        currentClient.forEach((key,value) -> {
            value.setPokers(new ArrayList<>());
        });
    }

    public static void refresh(){
        init();
        Collections.shuffle(leftPokers);
    }

    public static void deal(){
        refresh();
        List<String> temp = leftPokers;
        final int[] i = {0};
        currentClient.forEach((key,value) ->{
            value.setPokers(temp.subList(i[0], i[0] +17));
            i[0] += 17;
        });
        leftPokers = temp.subList(51,54);
    }

    //获取下一个出派人
    public static String getNext(String current) {
        int index = loop.indexOf(current);
        if(index == (loop.size()-1)) {
            return loop.get(0);
        } else {
            return loop.get(index+1);
        }
    }

    public static void shuffle(){
        deal();
    }

    public static void pull(String ip) {
        List<String> temp = copyList(currentClient.get(ip).getPokers());
        List<String> left = copyList(leftPokers);
        for (String item: left) {
            temp.add(item);
        }
        currentClient.get(ip).setPokers(temp);
        leftPokers = new ArrayList<>();
    }

    //出牌
    public static String push(String message, String ip) {
        String result = "";
        List<String> cards = Arrays.asList(message.split(","));
        List<String> temp = copyList(currentClient.get(ip).getPokers());
        if(!ip.equals(lastSend) && ((lastCards.size() != 0)&&(lastCards.size()!=cards.size()))) {
            if((cards.size() == 4 && (cards.get(0).equals(cards.get(3)))) || (cards.size() == 2 && (("Joker".equals(cards.get(0)))||("joker".equals(cards.get(0)))))) {
            } else {
                result = "error";
                return result;
            }
        }
        int times = 0;
        for(String card : cards) {
            temp.remove(card);
            times ++;
        }
        if(currentClient.get(ip).getPokers().size() == (temp.size()+times)) {
            if(temp.size() == 0) {
                result = "win";
                lastSend = "";
                lastCards = new ArrayList<>();
            } else {
                result = "success";
                lastSend = ip;
                lastCards = cards;
            }
            leftPokers.addAll(cards);
            currentClient.get(ip).setPokers(temp);
        } else {
            result = "error";
        }
        return result;
    }

    public static List<String> copyList(List<String> old) {
        List<String> newOne = new ArrayList<>();
        for(String item : old) {
            newOne.add(item);
        }
        return newOne;
    }
}