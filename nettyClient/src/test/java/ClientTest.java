import client.Client;

import java.util.Scanner;

public class ClientTest {

    public static void main(String[] args) {
        Client nettyClient = new Client();

        try {
            nettyClient.start();
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()) {
                String text = scanner.nextLine();
                nettyClient.send(text);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nettyClient.close();
        }
    }
}
