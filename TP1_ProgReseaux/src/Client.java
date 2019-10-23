
/* Classe client
   Instance client de cette classe créé lorsqu'un client se connecte au serveur
   Possède 2 attributs : 1 nom et son Socket
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private Socket socket;
    private String name;
    private Group groupe;

    Client(Socket s, String name) {
        this.socket = s;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public synchronized Group getGroup() {
        return this.groupe;
    }

    public synchronized void setGroup(Group g) {
        this.groupe = g;
    }

    public synchronized void setGroup() {
        this.groupe = null;
    }

    public Socket getSocket() {
        return this.socket;
    }

}
