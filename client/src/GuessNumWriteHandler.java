import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class GuessNumWriteHandler extends Thread{
    private Socket client;

    public GuessNumWriteHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            OutputStream out = client.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            Scanner scanner = new Scanner(System.in,"UTF-8");
            while (true) {
                String message = scanner.nextLine();
                printStream.println(message);
                printStream.flush();
                if(message.equals("bye")){
                    break;
                }
            }
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
