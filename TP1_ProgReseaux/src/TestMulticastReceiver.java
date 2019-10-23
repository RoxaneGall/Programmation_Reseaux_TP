
import java.net.*;
import java.io.*;

class TestMulticastReceiver {

    public static void main(String[] arg) throws Exception {
        String nom = arg[0];
        InetAddress groupeIP = InetAddress.getByName("239.255.80.84");
        int port = 8084;
        new MulticastReceiver(groupeIP, port, nom);
    }
}
