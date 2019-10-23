/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {

    static List<Group> allGroups = new ArrayList<>(); // tous les noms de groupes créés sont stockés dans un fichier (1 nom par ligne)

    private Client client;

    // On stocke dans un fichier les noms des différents groupes. Ici on met en attribut le flux de sortie car tous les threads doivent écrire avec ce même flux
    // on peut également mettre le flux de lecture car il n'est utilisé qu'une seule fois, losqu'on lit le fichier des groupesa udébut,
    // // ensuite c'est la liste des groupes qui est utilisés
    private static BufferedWriter groupWriter =null;
    private static BufferedReader groupReader = null;
    private static boolean read=false;

    ClientThread(Socket s) {
        this.client = new Client(s, "");
        try {
			this.groupWriter=new BufferedWriter(new FileWriter(new File("C:/Users/manal/Documents/INSA/4IF/S1/RESEAUX/TP1_ProgReseaux/TP1_ProgReseaux/ressources/allGroups.txt"),true));
		} catch (Exception e) {
			System.out.println(e);
		}
    }

    /*
     * receives a request from client then sends an echo to the client
     * @param clientSocket the client socket
     **/
    public void run() {
       if (groupWriter==null || groupReader==null || read==false) {
                // on lit les groupes existant sur le serveur
                readAllGroups();
                read=true; // Le client ne lit les groupes existants que la première fois qu'il se connecte
        }

        try {
            // Création du flux d'entrée et de sortie du serveur
            BufferedReader socIn = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream(), "UTF-8"));
            PrintStream socOut = new PrintStream(client.getSocket().getOutputStream());

            // on attribue son nom au client
            while (client.getName().equals("")) {
                String name = socIn.readLine();
                client.setName(name);
            }

            // On lit les messages écrits par le client et on les traite
            String line = "";
            Socket socket;
            while (true) {
                if (client.getGroup() == null) { // client n'est pas dans un groupe de discussion
                    line = socIn.readLine();
                    if (line.equals("1")) { // création d'un groupe
                        line = socIn.readLine(); // line est le nom du groupe
                        Group groupe = new Group(line);
                        addGroup(groupe);
                        client.setGroup(groupe);
                        groupe.addClient(client);
                    } else if (line.equals("2")) {// rejoint un groupe
                        if (getAllGroups().size() != 0) { // s'il existe des groupes
                            socOut.println(stringAllGroups()); // on envoie au client les groupes disponibles sur le serveur
                            line = socIn.readLine(); // lecture de l'indice du groupe choisi
                            Group groupe = allGroups.get(Integer.parseInt(line));
                            client.setGroup(groupe);
                            groupe.addClient(client);
                            socOut.println(groupe.getHistorique()); // on envoie au client l'historique du group
                        }
                    }
                } else { // cas classique d'envoie du message dans un groupe de discussion
                    line = socIn.readLine();
                    if (line.equals("menuGroup")) {// le client demande à changer de groupe
                        client.getGroup().removeClient(client);
                        client.setGroup();
                    } else if (!line.equals("null")){ // le client envoie un message
						System.out.println("*****line="+line);
                        for (Client c : client.getGroup().getlisteClients()) {
                            socket = c.getSocket();
                            socOut = new PrintStream(socket.getOutputStream());
                            if (this.client.getSocket() != socket) {
									socOut.println("    " + client.getName() + " : " + line);
							}
						}
                        client.getGroup().addMessage(client.getName() + " : " + line + "\r\n");
					}else if (line.equals("null")){ 
						socOut.println("    " + client.getName() + " a quitté la conversation.");
						client.getGroup().addMessage(client.getName() + " a quitté la conversation. \r\n");
					}
				}
			}
		} catch (Exception e) {
            System.err.println("Deconnected user socket :" + e);
        }
    }


    public static void readAllGroups() {
        try {
            groupReader = new BufferedReader(new InputStreamReader(new FileInputStream("C:/Users/manal/Documents/INSA/4IF/S1/RESEAUX/TP1_ProgReseaux/TP1_ProgReseaux/ressources/allGroups.txt")));
            System.out.println("Reading allGroups");

            String lineName = groupReader.readLine();
            System.out.println(lineName);
            while (lineName != null && !lineName.equals("")) {
                Group g = new Group(lineName);
                allGroups.add(g);
                lineName = groupReader.readLine();
            }
            groupReader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    static String stringAllGroups() {
        String stringAllGroups = "Les groupes disponibles sont \r\n";
        int i = 0;
        for (Group g : getAllGroups()) {
            stringAllGroups += i + ". Groupe : " + g.getName() + " (id " + g.getID() + ") \r\n";
            i++;
        }
        stringAllGroups += "Rejoindre le groupe numero : ";
        System.out.println("Tous les groupes : "+stringAllGroups);
        return stringAllGroups;
    }

    static synchronized void addGroup(Group g) {
        allGroups.add(g);
        try {
            System.out.println("Ecriture du groupe "+g.getName()+" dans le fichier");
            groupWriter.write(g.getName()+"\r\n");
            groupWriter.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(g.getName() + " ajouté à la liste des groupes");
    }
    static synchronized List<Group> getAllGroups() {
        return allGroups;
    }

}
