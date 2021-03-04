import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GuessNumHandler extends Thread{

    //储存所有注册的客户端
    private final Socket client;
    private String name;

    public GuessNumHandler(Socket server) {
        client = server;
    }

    @Override
    public void run() {
        try {
            //获取客户端输入流
            InputStream in = this.client.getInputStream();
            Scanner scanner = new Scanner(in,"UTF-8");
            while (true) {
                String line = scanner.nextLine();
                if(line.startsWith("c:") && (this.name!=null)) {
                    broadcast(this.name +"："+line.split(":")[1]);
                }
                if("q".equals(line)) {
                    Resource.status = "notStart";
                }
                //游戏中
                if("running".equals(Resource.status)) {
                    if("l".equals(line)) {
                        sendMessage(this.client,Resource.sb.toString());
                    } else {
                        if(!name.equals(Resource.currentPlayer)) {
                            sendMessage(this.client,"current ："+ Resource.currentPlayer);
                        }else {
                            broadcast(Resource.currentPlayer+" guess number："+ line);
                            Resource.currentPlayer = getNextPlayer();
                            boolean done = checkNum(line);
                            if(!done) {
                                broadcast("next："+Resource.currentPlayer);
                            }
                        }
                    }
                } else {
                    // 注册
                    if (line.startsWith("register:")) {
                        String[] segments = line.split(":");
                        if (segments.length == 2 && segments[0].equals("register")) {
                            String name = segments[1];
                            register(name);
                        }
                        continue;
                    }
                    // 观察者
                    if (line.startsWith("watch:")) {
                        String[] segments = line.split(":");
                        if (segments.length == 2 && segments[0].equals("watch")) {
                            String name = segments[1];
                            watch(name);
                        }
                        continue;
                    }
                    //开始
                    if(line.startsWith("start:") && (this.client == Resource.host)) {
                        Resource.currentPlayer = line.split(":")[1];
                        if(Resource.socketMap.containsKey(Resource.currentPlayer)) {
                            generate();
                            broadcast("guess number start！");
                            Resource.status = "running";
                        } else {
                            sendMessage(this.client,"不存在当前"+ Resource.currentPlayer + "玩家");
                        }
                    }
                    //查看当前人数
                    if("players".equals(line)) {

                    }
                    //退出
                    if (line.equalsIgnoreCase("exit")) {
                        quit();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNum(String next) {
        boolean flag = false;
        if(next.length()!=6){
            broadcast("error，"+name+" input：");
            Resource.currentPlayer = name;
        } else {
            int complete = 0;
            AtomicInteger num = new AtomicInteger();
            Map<Integer,Integer> right = new HashMap<>();
            try{
                for(int i=0;i<next.length();i++) {
                    Integer tmp = Integer.parseInt(String.valueOf(next.charAt(i)));
                    if(Resource.list[i] == tmp) {
                        complete++;
                    }
                    if(Resource.map.get(tmp) != null) {
                        if(right.get(tmp) == null) {
                            right.put(tmp, 1);
                        } else {
                            if(Resource.map.get(tmp) > right.get(tmp)){
                                right.put(tmp, right.get(tmp)+1);
                            }
                        }
                    }
                }
                right.forEach((item,value) -> num.addAndGet(value));
                broadcast("complete right："+ complete+",right num："+num);
                Resource.sb.append(next).append("    ").append(complete).append("/").append(num).append("\r\n");
                right.clear();
                if(complete == 6) {
                    broadcast("done！");
                    Resource.status = "done";
                    flag = true;
                }
            } catch (NumberFormatException e) {
                broadcast("error，"+name+" input：");
                Resource.currentPlayer = name;
            }
        }
        return flag;
    }

    private void quit() {
        if(Resource.host == this.client) {
            broadcast("房主"+this.name+"下线了！");
            Resource.host = null;
            if(!Resource.socketMap.isEmpty()){
                AtomicBoolean flag = new AtomicBoolean(true);
                Resource.socketMap.forEach((key, value) -> {
                    if(!this.name.equals(key) && flag.get()) {
                        flag.set(false);
                        Resource.host = value;
                        broadcast("玩家"+key+"被选为房主！");
                    }
                });
            }
        }else {
            broadcast("玩家"+this.name+"下线了！");
        }
        Resource.socketMap.remove(this.name);
        Resource.players.remove(this.name);
        System.out.println(this.name + "下线了");
        printOnlineClient();
    }

    private void register(String name) {
        this.name = name;
        System.out.println(name + "注册到系统中");
        sendMessage(this.client, "欢迎，" + name + "注册成功");
        broadcast("玩家"+name+"进入游戏");
        if(Resource.socketMap.isEmpty()) {
            Resource.host = this.client;
            sendMessage(this.client, "你是第一个玩家，成为房主");
        }
        Resource.socketMap.put(name,this.client);
        Resource.players.add(name);
        printOnlineClient();
    }

    private void watch(String name) {
        this.name = name;
        System.out.println(name + "注册到系统中");
        sendMessage(this.client, "欢迎，" + name + "注册成功");
        broadcast("观察者"+name+"进入游戏");
        Resource.socketMap.put(name,this.client);
        printOnlineClient();
    }

    //打印当前在线客户人数
    private void printOnlineClient() {
        System.out.println("当前在线的玩家有：" + Resource.socketMap.size() + "个，名称列表如下：");
        for(String name:Resource.socketMap.keySet()) {//keySet()取得所有的key信息
            System.out.println(name);
        }
    }
    //发指定消息到客户端
    private void sendMessage(Socket client, String s) {
        try {
            OutputStream out = client.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(s);
            System.out.println(s);
            printStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 广播给所有人信息
    private void broadcast(String message) {
        Resource.socketMap.forEach((key,value) -> {
            sendMessage(value,message);
        });
    }

    //生成数字
    private void generate() {
        Resource.list = new Integer[6];
        for(int i=0;i<6;i++) {
            Resource.list[i] = Integer.parseInt((Math.random()*10+"").substring(0,1));
        }
        Resource.map.clear();
        Arrays.stream(Resource.list).forEach(item -> {
            if(Resource.map.get(item) == null) {
                Resource.map.put(item, 1);
            } else {
                Resource.map.put(item,Resource.map.get(item)+1);
            }
        });
        Resource.sb = new StringBuffer();
    }

    //获取下一个客户端
    private Socket getNext() {
        int index = Resource.players.indexOf(name);
        if(index == Resource.players.size()+1) {
            return Resource.socketMap.get(Resource.players.get(0));
        } else {
            return Resource.socketMap.get(Resource.players.get(index+1));
        }
    }

    //获取下一个玩家名称
    private String getNextPlayer(){
        int index = Resource.players.indexOf(name);
        if(index == (Resource.players.size()-1)) {
            return Resource.players.get(0);
        } else {
            return Resource.players.get(index+1);
        }
    }
}
