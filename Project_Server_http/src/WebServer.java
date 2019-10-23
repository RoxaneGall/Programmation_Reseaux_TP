import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Server HTTP implémentant les requetes GET, HEAD, PUT, POST et DELETE.
 * @author Roxane GALL, Manal EL KARCHOUNI
 * @version 1.0
 */
public class WebServer {

    /* Chemin absolu du repertoire des fichiers ressources utilisés par le server (fichiers statique de tout format (texte, html, média...)) */
    protected static final String resourceDirectory = "C:/Users/Rox'/IntelliJIDEAProjects/Project_Server_http/resources/";
    /* Chemin absolu de la page web envoyee en cas d'erreur 404 */
    protected static final String fileNotFound = "C:/Users/Rox'/IntelliJIDEAProjects/Project_Server_http/files/pageNotFound.html";
    /* Chemin absolu de la page d'acceuil/index du serveur */
    protected static final String index = "C:/Users/Rox'/IntelliJIDEAProjects/Project_Server_http/files/index.html";

    /** WebServer constructor. */
    protected void start() {
        ServerSocket s;

        System.out.println("Webserver starting up on port 3000");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        /** Le serveur écoute sur son port, en attente de connexion... */
        System.out.println("Waiting for connection");
        Socket remote = null;
        BufferedOutputStream outBytes = null;
        for (; ; ) {
            try {
                // wait for a connection
                remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                System.out.println("Remote : " + remote);

                // Ouverture d'un flux d'écriture pour permettre ensuite d'envoyer en bytes du contenu au client
                outBytes = new BufferedOutputStream(remote.getOutputStream());
                // Ouverture d'un flux de lecture pour lire en string/ligne par ligne la requête envoyée par le client
                BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));

                /** Lecture des données envoyées par le client
                 * We basically ignore it,
                 * stop reading once a blank line is hit. This
                 * blank line signals the end of the client HTTP
                 * headers.
                 */

                String line = in.readLine();
                String header = "";

                /** Parcourt du header jusqu'à sa fin
                 On considère ici que le format du header est respecté, s'il se finit par une ligne vide / pas d'autre verif
                 Sinon on aurait pu générer une erreur 400 bad request si le format n'était pas respecté */
                header = line;
                while (!line.equals("") && line != null) {
                    header += line;
                    line = in.readLine();
                    System.out.println(line);
                }

                /** On traite le requête dans une méthode séparée */
                handleRequest(header, outBytes, in);

                remote.close();

            } catch (Exception e) { // Erreur lors de la connexion du client
                System.out.println(e);
                try { // essai de prévenir le client, pas sûr que le message arrive à destination
                    outBytes.write(sendHeader("500 Internal Server Error").getBytes());
                    outBytes.flush();
                    remote.close();
                } catch (Exception e2) {
                }
                ;
            }
        }
    }

    /** Traitement de la requête du client
     * Le serveur implémente ici les requêtes de type GET, PUT, POST, HEAD et DELETE
     * @param header en-tete de la requete
     * @param outBytes flux d'écriture en byte sur le socket Client
     * @param in flux de lecture des données envoyées par le client, ce buffer a déjà lu l'en-tete
     */
    public void handleRequest(String header, BufferedOutputStream outBytes, BufferedReader in) {
        String[] params = header.split(" ");
        String method, path;

        method = params[0]; // GET, POST, PUT, HEAD, ou DELETE
        path = params[1].substring(1); // target resource, on enlève le / qui précède le nome de la ressource
        System.out.println(method + " " + path + ".");

        /** On traite différement la requête suivant son type */
        switch (method) {
            case "GET":
                getRequest(path, outBytes);
                break;
            case "HEAD":
                headRequest(outBytes, path);
                break;
            case "POST":
                postRequest(path, outBytes, in);
                break;
            case "PUT":
                putRequest(path, outBytes, in);
                break;
            case "DELETE":
                deleteRequest(path, outBytes);
                break;
            default: // cas d'une requete non implémentée sur notre serveur (par exemple VIEW)
                try {
                    outBytes.write(sendHeader("501 Not Implemented").getBytes());
                    outBytes.flush();
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
    }

    /**
     * Implémentation du traitement d'une requete GET - cette méthode retourne une page WEB identifiée par son URL
     * Tente d'ouvrir et de lire la ressource demandee et de l'envoyer au client, sous forme de bytes.
     * /!\ on aurait pu envoyer sous forme de string les fichiers txt ou html... mais ici la méthode est générale et peut aussi être amené à envoyer des medias
     * On renvoie le code 200 OK si le fichier a ete trouve et 404 Not Found sinon.
     * Le corps de la reponse est le contenu du fichier, transmis en bytes, ou bien le contenu de la page fileNotFound du serveur
     *
     * @param outBytes  Flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
     * @param path Chemin du fichier que le client veut consulter.
     */
    public void getRequest(String path, BufferedOutputStream outBytes) {
        try {
            if (path.equals("")) {
                path = index; // si rien n'est demandé on renvoie le fichier index
            } else {
                path = resourceDirectory + path; // si une resource est demandée on la recherche dans le répertoire de resssources du serveur
            }
            // un fichier est demandé
            File resource = new File(path);
            if (resource.exists() && resource.isFile()) { // Si la ressource demandée existe
                outBytes.write(sendHeader("200 OK", path, resource.length()).getBytes());
                /** Envoie du contenu du fichier */
                sendFile(outBytes, resource);
            } else { // la ressource n'existe pas, on envoie l'erreur 404
                resource = new File(fileNotFound);
                outBytes.write(sendHeader("404 Not Found", fileNotFound, resource.length()).getBytes());
                sendFile(outBytes, resource);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Implémentation du traitement d'une requete POST  - cette méthode envoie du contenu au serveur qui le stocke à l'adresse spécifiée / remplace son contenue
     * s'il y a déjà une ressource existante à cet emplacement
     * Tente de créer la ressource indiquee, de lire le corps de la requete et d'écrire ce contenu dans le fichier ressource créé
     * @param out flux d'écriture vers le socket client pour lui renvoyer une en-tête / un header
     * @param in  flux de lecture du socket client, dont on veut lire le corps / body
     * @param path chemin du fichier que le client veut creer ou editer.
     */

    public void putRequest(String path, BufferedOutputStream out, BufferedReader in) {
        try {
            File resource = new File(resourceDirectory+path);
            boolean newFile = resource.createNewFile(); // newFile vaut vrai si le fichier est créé

            if(newFile) { // si le fichier existe déjà, on supprime son contenu
                PrintWriter pw = new PrintWriter(resource);
                pw.close();
            }

            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(resource)); // Ouverture d'un flux d'ecriture binaire vers le fichier
            /** Parcourt des informations recues dans le body de la requete PUT dans le fichier destination */
            byte[] buffer = new byte[1024];
            String lineBody = in.readLine();
            while (lineBody!=null && !lineBody.equals("")) {
                System.out.println("line :"+lineBody);
                lineBody = in.readLine();
                fileOut.write(lineBody.getBytes(), 0, lineBody.getBytes().length);
                fileOut.write("\r\n".getBytes(), 0, "\r\n".getBytes().length);
            }
            fileOut.flush(); // écriture des données
            fileOut.close();  // fermeture flux ecriture

            if (newFile) {
                out.write(sendHeader("201 Created").getBytes()); // si le fichier est nouveau
                out.write("\r\n".getBytes());
            } else {
                out.write(sendHeader("200 OK").getBytes()); // si le fichier existait déjà
                out.write("\r\n".getBytes());
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.write(sendHeader("500 Internal Server Error").getBytes());
                out.write("\r\n".getBytes());
                out.flush();
            } catch (Exception e2) {
                System.out.println(e);
            }
        }
    }

    /**
     * Envoie d'une réponse à une requete POST - Implementation de la methode HTTP POST.
     * Similaire à la méthode putRequest à la seule différence que dans le cas d'édition d'un fichier existant
     * post écrit le contenu à la suite de celui du fichier et ne l'écrase pas
     */
    public void postRequest(String path,BufferedOutputStream outBytes, BufferedReader in) {
        // Similaire à put sauf qu'on n'écrase pas le contenu du fichier
        try {
            File resource = new File(resourceDirectory + path);
            boolean newFile = resource.createNewFile(); // newFile vaut vrai si le fichier est créé

            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(resource,resource.exists())); // Ouverture d'un flux d'ecriture binaire vers le fichier
            /** Parcourt des informations recues dans le body de la requete PUT dans le fichier destination */
            byte[] buffer = new byte[1024];
            String lineBody = in.readLine();
            while (lineBody!=null && !lineBody.equals("")) {
                System.out.println("line :"+lineBody);
                lineBody = in.readLine();
                fileOut.write(lineBody.getBytes(), 0, lineBody.getBytes().length);
                fileOut.write("\r\n".getBytes(), 0, "\r\n".getBytes().length);
            }
            fileOut.flush(); // écriture des données
            fileOut.close();  // fermeture flux ecriture

            if (newFile) {
                outBytes.write(sendHeader("201 Created").getBytes()); // si le fichier est nouveau
                outBytes.write("\r\n".getBytes());
            } else {
                outBytes.write(sendHeader("200 OK").getBytes()); // si le fichier existait déjà
                outBytes.write("\r\n".getBytes());
            }
            outBytes.flush();
        } catch (Exception e){
            e.printStackTrace();
            try {
                outBytes.write(sendHeader("500 Internal Server Error").getBytes());
                outBytes.write("\r\n".getBytes());
                outBytes.flush();
            } catch (Exception e2) {
                System.out.println(e);
            }

        }
    }

    /**
     * Envoie d'une réponse à une requete HEAD - Implementation de la methode HTTP HEAD.
     * a method to return the head of the Web page identified by
     * the URL (the head contains summary information such as title,
     * date of creation, etc.
     * /!\ IL FAUDRAIT AUSSI RENVOYER LE TYPE DE CONTENU LE REFERRER POLICY, LA DATE etc.
     * @param outBytes  Flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
     * @param path Chemin du fichier que le client veut consulter.
     */
    protected void headRequest(BufferedOutputStream outBytes, String path) {
        try {

            // Verification de l'existence de la ressource demandee
            File resource = new File(resourceDirectory+path);
            if(resource.exists() && resource.isFile()) {
                outBytes.write(sendHeader("200 OK", path, resource.length()).getBytes());
                outBytes.write("\r\n".getBytes());
            } else {
                outBytes.write(sendHeader("404 Not Found").getBytes());
                outBytes.write("\r\n".getBytes());
            }
            // Envoi des donnees
            outBytes.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                outBytes.write(sendHeader("500 Internal Server Error").getBytes());
                outBytes.write("\r\n".getBytes());
                outBytes.flush();
            } catch (Exception e2) {};
        }
    }



    /**
     * Envoie d'un fichier (sous forme de bytes) dont le flux d'écriture est passé en paramètre
     *
     * @param resource fichier que le client veut consulter.
     *                 out Flux d'écriture sur lequel envoyé les bytes du fichier
     * @param out flux d'écritre en bytes vers le socket client
     * @return byte[] Bytes qui constituent le fichier lu / contenu du fichier sous forme de byte
     */
    public void sendFile(BufferedOutputStream out, File resource) {
        try {
            // Ouverture d'un flux de lecture binaire sur le fichier demande
            BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(resource));
            // Envoi du corps : le fichier (page HTML, image, video...)
            byte[] buffer = new byte[256];
            int nbRead = fileIn.read(buffer);
            while (nbRead!= -1) {
                out.write(buffer, 0, nbRead);
                nbRead = fileIn.read(buffer);
            }
            fileIn.close(); // Fermeture du flux de lecture
            out.flush(); //Envoi des donnees
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Envoie d'une réponse à une requete DELETE - Implementation de la methode HTTP DELETE.
     * Cette méthode a pour but de supprimer la ressource indiquee par le client
     *
     * @param outBytes flux d'ecriture binaire vers le socket client auquel il faut envoyer une reponse.
     * @param path chemin du fichier que le client veut supprimer.
     */
    public void deleteRequest(String path, BufferedOutputStream outBytes) {
        try {
            File resource = new File(resourceDirectory+path);
            // Suppression du fichier
            boolean deleted = false;
            boolean existed = false;
            if((existed = resource.exists()) && resource.isFile()) {
                deleted = resource.delete();
            }

            // Envoi du Header
            if(deleted) {
                outBytes.write(sendHeader("204 No Content").getBytes());
                outBytes.write("\r\n".getBytes());
            } else if (!existed) {
                outBytes.write(sendHeader("404 Not Found").getBytes());
                outBytes.write("\r\n".getBytes());
            } else {
                // Le fichier a ete trouve mais n'a pas pu etre supprime
                outBytes.write(sendHeader("403 Forbidden").getBytes());
                outBytes.write("\r\n".getBytes());
            }
            // Envoi des donnees
            outBytes.flush();
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur on essaie d'avertir le client
            try {
                outBytes.write(sendHeader("500 Internal Server Error").getBytes());
                outBytes.write("\r\n".getBytes());
                outBytes.flush();
            } catch (Exception e2) {};
        }
    }

    /**
     * Cette methode renvoie une en-tête HTML simple, pour une reponse qui n'a pas de corps.
     * L'en-tete cree contient un code de retour et precise le type du serveur : Bot.
     *
     * @param status le code de reponse HTML a fournir dans l'en-tete.
     * @return l'en-tete de reponse HTML.
     */
    protected String sendHeader(String status) {
        String header = "HTTP/1.0 " + status + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        System.out.println(header);
        return header;
    }

    /**
     * Cette methode permet de creer un en-tete de reponse HTML, pour une reponse qui aura un corps.
     * L'en-tete cree contient un code de retour et precise le type du serveur : Bot, le type de contenu du corps et la taille du corps en bytes.
     *
     * @param status   Le code de retour.
     * @param filename Le chemin vers la ressource associee et la reponse, dont le contenu sera renvoye dans le corps de la reponse.
     * @return L'en-tete de reponse HTML.
     */
    protected String sendHeader(String status, String filename, long length) {
        String header = "HTTP/1.0 " + status + "\r\n";
        if (filename.endsWith(".html") || filename.endsWith(".htm"))
            header += "Content-Type: text/html\r\n";
        else if (filename.endsWith(".mp4"))
            header += "Content-Type: video/mp4\r\n";
        else if (filename.endsWith(".png"))
            header += "Content-Type: image/png\r\n";
        else if (filename.endsWith(".jpeg") || filename.endsWith(".jpeg"))
            header += "Content-Type: image/jpg\r\n";
        else if (filename.endsWith(".mp3"))
            header += "Content-Type: audio/mp3\r\n";
        else if (filename.endsWith(".avi"))
            header += "Content-Type: video/x-msvideo\r\n";
        else if (filename.endsWith(".css"))
            header += "Content-Type: text/css\r\n";
        else if (filename.endsWith(".pdf"))
            header += "Content-Type: application/pdf\r\n";
        else if (filename.endsWith(".odt"))
            header += "Content-Type: application/vnd.oasis.opendocument.text\r\n";
        header += "Content-Length: " + length + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        System.out.println("ANSWER HEADER :");
        System.out.println(header);
        return header;
    }

    /**
     * Start the application.
     *
     * @param args Command line parameters are not used.
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
