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
    private static Integer freeClientConnections;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = null;
            ConnectionPool.initializePool();

            ResourceBundle configFile = ResourceBundle.getBundle("configuration.config");
            Integer port = Integer.valueOf(configFile.getString("Port"));
            freeClientConnections = Integer.valueOf(configFile.getString("MaxServerConnections"));
            serverSocket = new ServerSocket(port);
            
            Logger.getLogger(ServerApplication.class.getName()).log(Level.INFO, "Server listening on port: {0}", serverSocket.getLocalPort());

            //Accept connections and start a listener thread through Worker.
            while(true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if(freeClientConnections > 0){
                        ServerWorker serverWorker = new ServerWorker(clientSocket, Boolean.TRUE);
                        serverWorker.start();
                    }else{
                        //This worker will reject the client by sending an error message.
                        ServerWorker serverWorker = new ServerWorker(clientSocket, Boolean.FALSE);
                        serverWorker.start();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerApplication.class.getName()).log(Level.SEVERE, "IOException: {0}", ex.getMessage());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerApplication.class.getName()).log(Level.SEVERE, "IOException: {0}", ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(ServerApplication.class.getName()).log(Level.SEVERE, "SQLException: {0}", ex.getMessage());
        }
    }
  /**
   * -- to free client connections
   */
    public static synchronized void useClientConnection(){
        freeClientConnections--;
    }
   /**
   * ++ to free client connections
   */
    public static synchronized void releaseClientConnection(){
        freeClientConnections++;
    }
}
