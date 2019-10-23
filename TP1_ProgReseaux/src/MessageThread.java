
import java.io.*;
import java.net.*;
import java.util.*;

public class MessageThread
        extends Thread {

    private BufferedReader in;

    MessageThread(BufferedReader in) {
        this.in = in;
    }

    /**
     * receives a request from client then sends an echo to the client
  *
     */
    public void run() {
        try {
            while (true) {
                String msg = in.readLine();
                System.out.println(msg);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

}
