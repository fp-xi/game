import client.Client;

import java.util.Scanner;

public class ClientStart {

    public static void main(String[] args) {
        Client nettyClient = new Client();
        System.out.println("**** Hello I want to play a game!****");
        System.out.println("******** Input start to play! *******");
        System.out.println("***** Input c for landlord **********");
        System.out.println("******** w for give up ***************");
        System.out.println("********** p for pass ***************");
        System.out.println("********* : for chat ****************");
        System.out.println("*************************************");
        System.out.println("\033[31;4mRed Underline Text\033[0m");

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
