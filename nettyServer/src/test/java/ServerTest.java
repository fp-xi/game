import server.Server;

public class ServerTest {

    public static void main(String[] args) {
        Server nettyServer = new Server();
        nettyServer.start();
    }
}
