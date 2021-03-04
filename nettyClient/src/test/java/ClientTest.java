import client.Client;
import org.apache.commons.lang3.RandomStringUtils;

public class ClientTest {

    public static void main(String[] args) {
        Client nettyClient = new Client();
        nettyClient.start();

        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(2000);
                String response = nettyClient.send(RandomStringUtils.random(32, true, true));
                System.out.println("response:" + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            nettyClient.close();
        }
    }
}
