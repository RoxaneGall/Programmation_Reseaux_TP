
import java.net.*;
import java.io.*;

public class MulticastWriter {

    InetAddress groupeIP;
    int port;
    MulticastSocket socketEmission;
    String nom;

    MulticastWriter(InetAddress groupeIP, int port, String nom) throws Exception {
        this.groupeIP = groupeIP;
        this.port = port;
        this.nom = nom;
        socketEmission = new MulticastSocket();
        socketEmission.setTimeToLive(15); 
        run();
    }

    public void run() {
        System.out.println("You're ready to send messages to the group : [nom : " + nom + ", groupIP : " + groupeIP + " port : " + port + "]");
        BufferedReader entreeClavier;
        try {
            entreeClavier = new BufferedReader(new InputStreamReader(System.in));
            String texte = entreeClavier.readLine();
            while (!texte.equals("STOP")) {
                emettre(texte);
                texte = entreeClavier.readLine();
            }
        } catch (Exception exc) {
            System.out.println(exc);
        }
    }

    void emettre(String texte) throws Exception {
        byte[] contenuMessage;
        DatagramPacket message;

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        texte = nom + " : " + texte;
        (new DataOutputStream(sortie)).writeUTF(texte);
        contenuMessage = sortie.toByteArray();
        message = new DatagramPacket(contenuMessage, contenuMessage.length, groupeIP, port);
        socketEmission.send(message);

    }
}
