import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        String ip = "121.4.70.234";//服务地址
        //String ip = "127.0.0.1";
        int port = 1314;
        try {
            System.out.println("*************************Welcome guess number：");
            System.out.println("**input ”register:name“ to register in system；");
            System.out.println("********The host is the one who first join in：");
            System.out.println("**********************input“start:name” begin：");
            System.out.println("*********************************************");
            final Socket socket = new Socket(ip,port);
            new GuessNumWriteHandler(socket).start();
            new GuessNumReadHandler(socket).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
