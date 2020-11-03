package application;

import control.ConnectionPool;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server entry point.
 * @author aitor
 */
public class ServerApplication {
    private static final int PORT = 5005;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        ConnectionPool.initializePool();
        
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
        
        System.out.println("Server listening on port: " + serverSocket.getLocalPort());
        
        //Accept connections and start a listener thread through Worker.
        Socket clientSocket = null;
        while(true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException ex) {
                System.out.println("IOException: " + ex.getMessage());
            }
            new ServerWorker(clientSocket).start();
        }
    }
}
