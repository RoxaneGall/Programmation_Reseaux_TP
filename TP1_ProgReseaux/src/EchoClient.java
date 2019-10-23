
/** *
 * EchoClient
 * Example of a TCP client
 * Date: 10/01/04
 */

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.net.*;

public class EchoClient {

    /**
     * main method accepts a connection, receives a message from client then
     * sends it to the server
     *
     */
    public static void main(String[] args) throws IOException {

        Socket clientSocket = null;
        PrintStream socOut = null;
        BufferedReader stdIn = null;
        BufferedReader socIn = null;

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            // Création du socket client associé au port et au serveur précisés en arg, le serveur crée le clientThread
            clientSocket = new Socket(args[0], new Integer(args[1]).intValue());
            System.out.println("Création du socket " + clientSocket + " côté client.");

            // Initialisation du flux d'entrée, de sortie et du flux d'entrée clavier du socket côté client
            socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            socOut = new PrintStream(clientSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host : " + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to : " + args[0]);
            System.exit(1);
        }

        // On envoie au serveur le nom du client pour qu'il crée l'instance Client
        String name = "";
        while (name.equals("")) {
            System.out.println("Quel est ton petit nom ?");
            name = stdIn.readLine();
        }
        System.out.println(name + " ? J'adore ce nom ! \r\n");
        socOut.println(name);

        //
        MessageThread mt = new MessageThread(socIn);
        mt.start();

        String line = "";
        String groups = "";
        String numGroupe = ""; // groupe du client
        while (!line.equals(".")) {
            if (numGroupe.equals("")) { // cas où le client doit créer ou rejoindre un groupe de discussion
                while (!line.equals("1") && !line.equals("2")) {
                    System.out.println("\r\n1. Créer un groupe de discussion \r\n2. Rejoindre un groupe de discussion \r\n");
                    line = stdIn.readLine();
                }
                socOut.println(line);
                if (line.equals("1")) {
                    System.out.println("Nom du groupe : ");
                    line = stdIn.readLine();
                    socOut.println(line);
                    numGroupe = line;
                    System.out.println("Vous pouvez quitter à tout moment en écrivant \".\" et changer de groupe en écrivant \"menuGroup\"\r\n     ");
                } else if (line.equals("2")) {
                    BufferedReader groupReader = new BufferedReader(new InputStreamReader(new FileInputStream("C:/Users/manal/Documents/INSA/4IF/S1/RESEAUX/TP1_ProgReseaux/TP1_ProgReseaux/ressources/allGroups.txt")));
                    if (groupReader.readLine() == null) {
                        System.out.println("Aucun groupe n'est disponible.");
                        line = "";
                    } else {
                        line = socIn.readLine();
                        System.out.println(line);
                        line = stdIn.readLine(); // attend le numéro de choisi par le client
                        socOut.println(line);
                        numGroupe = line;
                        System.out.println("Vous pouvez quitter à tout moment en écrivant \".\" et changer de groupe de discussion en écrivant \"menuGroup\"\r\n     ");
                        line = socIn.readLine(); // attente de l'historique du groupe du Serveur
                        System.out.println("Historique : " + line);
                    }
                }
            } else { // cas classique de discussion dans un chat
                line = stdIn.readLine();
                socOut.println(line);
                if (line.equals("menuGroup") || line == null) {
                    numGroupe = "";
                    line = "quitte la conversation";

                }
            }
        }

        /* Déconnexion du client */
        socOut.close();
        socIn.close();
        stdIn.close();
        clientSocket.close();
    }
}
