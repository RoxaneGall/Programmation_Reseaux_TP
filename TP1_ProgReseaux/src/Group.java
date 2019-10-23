
import java.io.*;
import java.net.*;
import java.util.*;

public class Group {

    private static int ID = 0;
    private int id;
    private String name;
    private List<Client> listeClients = new ArrayList<>();

    // Chaque groupe a un fichier avec son historique de message pour lequel on stocke les flux d'écriture et de lecture
    private String filePath;
    private BufferedWriter historiqueWriter = null;
    private BufferedReader historiqueReader = null;

    Group(String name) {
        this.name = name;
        this.addID();
        this.id = getID();
        try {
            this.filePath = "C:/Users/manal/Documents/INSA/4IF/S1/RESEAUX/TP1_ProgReseaux/TP1_ProgReseaux/ressources/" + name + "_" + id + ".txt";
            File file = new File(filePath);
            if (file.createNewFile()) { // si le fichier n'existe pas déjà on le crée
                System.out.println(filePath + " File Created");
            } else {
                System.out.println("File " + filePath + " already exists");
            }

            historiqueWriter = new BufferedWriter(new FileWriter(new File(filePath), true)); // on récupère le flux d'écriture de ce groupe
            // dès qu'on veut écrire dans le fichier du groupe, le flux d'écriture est le même, on utilise l'attribut
            // ATTENTION ce n'est pas le cas pour la lecture puisqu'on veut lire le fichier depuis le début à chaque fois, le flux de lecture est donc initialisé
            // lorsqu'il est utilisé, plus loin, dans la méthode getHistorique()

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized BufferedWriter getWriter() {
        return historiqueWriter;
    }

    public synchronized BufferedReader getReader() {
        return historiqueReader;
    }

    public synchronized int getID() {
        return ID;
    }

    public synchronized void deleteID() {
        ID--;
    }

    public synchronized void addID() {
        ID++;
    }

    public synchronized List<Client> getlisteClients() {
        return listeClients;
    }

    public synchronized void removeClient(Client c) {
        listeClients.remove(c);
    }

    public synchronized void addClient(Client c) {
        listeClients.add(c);
        System.out.println(c.getName() + " a été ajouté au groupe " + name);
    }

    public synchronized String getHistorique() {

        String historique = "";
        try {
            historiqueReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            System.out.println("Reading File Historique line by line");
            String line = historiqueReader.readLine();
            while (line != null) {
                historique += "\r\n" + line;
                line = historiqueReader.readLine();
            }
            historiqueReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        return historique;
    }

    public synchronized void addMessage(String msg) {
        try {
            System.out.println("Ecriture du message " + msg + "dans le fichier");
            historiqueWriter.write(msg);
            historiqueWriter.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
