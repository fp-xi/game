import server.Server;

public class ServerStart {

    public static void main(String[] args) {
        Server nettyServer = new Server();
        nettyServer.start();
    }
}
