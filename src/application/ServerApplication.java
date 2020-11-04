package application;

import control.ConnectionPool;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server entry point.
 * @author aitor
 */
public class ServerApplication {
    private static Integer freeClientConnections = 10;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = null;
            ConnectionPool.initializePool();

            ResourceBundle configFile = ResourceBundle.getBundle("configuration.config");
            Integer port = Integer.valueOf(configFile.getString("Port"));
            serverSocket = new ServerSocket(port);

            System.out.println("Server listening on port: " + serverSocket.getLocalPort());

            //Accept connections and start a listener thread through Worker.
            while(true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("server accept");
                    if(freeClientConnections > 0){
                        ServerWorker serverWorker = new ServerWorker(clientSocket, Boolean.TRUE);
                        serverWorker.start();
                    }else{
                        //This worker will reject the client by sending an error message
                        ServerWorker serverWorker = new ServerWorker(clientSocket, Boolean.FALSE);
                        serverWorker.start();
                    }
                } catch (IOException ex) {
                    System.out.println("IOException: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(ServerApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void useClientConnection(){
        freeClientConnections--;
    }
    public static void releaseClientConnection(){
        freeClientConnections++;
    }
}
