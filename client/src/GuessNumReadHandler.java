import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class GuessNumReadHandler extends Thread{
    private Socket client;

    public GuessNumReadHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream in = client.getInputStream();
            Scanner scanner = new Scanner(in,"UTF-8");
            while (true) {
                try {
                    String message = scanner.nextLine();
                    System.out.println("server>>>" +
                            message);
                }catch (NoSuchElementException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

