/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import control.ConnectionPool;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;

/**
 * Server entry point.
 * @author aitor
 */
public class ServerApplication {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        ConnectionPool.initializePool();
        ResourceBundle configFile = ResourceBundle.getBundle("configuration.config");
        Integer port = Integer.valueOf(configFile.getString("Port"));
        try {
            serverSocket = new ServerSocket(port);
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
