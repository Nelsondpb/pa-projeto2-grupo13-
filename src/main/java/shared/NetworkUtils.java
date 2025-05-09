package shared;

import javax.net.ssl.SSLSocket;
import java.io.*;

public class NetworkUtils {
    public static void sendObject(SSLSocket socket, Object obj) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(obj);
        out.flush();
    }

    public static Object receiveObject(SSLSocket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return in.readObject();
    }
}