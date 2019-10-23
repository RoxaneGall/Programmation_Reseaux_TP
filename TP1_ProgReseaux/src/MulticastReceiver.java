
import java.net.*;
import java.io.*;

public class MulticastReceiver extends Thread {

    InetAddress groupeIP;
    int port;
    String nom;
    MulticastSocket socketReception;

    MulticastReceiver(InetAddress groupeIP, int port, String nom) throws Exception {
        this.groupeIP = groupeIP;
        this.port = port;
        this.nom = nom;
        socketReception = new MulticastSocket(port);
        socketReception.joinGroup(groupeIP);
        run();

    }

    public void run() {
        System.out.println("You just joined the group : [nom : " + nom + ", groupIP : " + groupeIP + " port : " + port + "]");
        System.out.println("Receiver ready");
        DatagramPacket message;
        byte[] contenuMessage;
        String texte;
        ByteArrayInputStream lecteur;

        while (true) {
            contenuMessage = new byte[1024];
            message = new DatagramPacket(contenuMessage, contenuMessage.length);
            try {

                socketReception.receive(message);
                texte = (new DataInputStream(new ByteArrayInputStream(contenuMessage))).readUTF();
                if (!texte.startsWith(nom)) {
                    continue;
                }
                System.out.println("Message received from " + texte);
            } catch (Exception exc) {
                System.out.println(exc);
            }
        }
    }
}
