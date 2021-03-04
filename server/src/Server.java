import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        int port = 1314;
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务端启动，运行在：" + serverSocket.getLocalSocketAddress());
            while (true) {
                final Socket socket = serverSocket.accept();
                System.out.println("客户端连接，来自：" + socket.getRemoteSocketAddress());
                executorService.execute(new GuessNumHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
