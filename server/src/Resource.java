import java.net.Socket;
import java.util.*;

public class Resource {

    public static String status = "notStart";
    public static final Map<String, Socket> socketMap = new LinkedHashMap<>();
    public static List<String> players = new ArrayList<>();
    public static String currentPlayer;
    public static Socket host;
    public static Integer[] list = new Integer[6];
    public static Map<Integer, Integer> map = new HashMap<>();
    public static StringBuffer sb = new StringBuffer();

}
