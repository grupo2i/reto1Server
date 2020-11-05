/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * delivers connections when asked to.
 * @author aitor
 */
public class ConnectionPool {
    private static Integer totalDBConnections;
    
    private static ArrayList<Connection> freeConnections = new ArrayList<>();
    private static ArrayList<Connection> usedConnections = new ArrayList<>();
    
    private static ResourceBundle propertiesFile = null;
    
    /**
     * initializes the pool to congif file
     * @throws SQLException 
     */
    public static void initializePool() throws SQLException{
        propertiesFile = ResourceBundle.getBundle("configuration.config");
        totalDBConnections = Integer.valueOf(propertiesFile.getString("MaxDBConnections"));
        for(int i = 0; i < totalDBConnections; i++){
            freeConnections.add(DriverManager.getConnection(propertiesFile.getString("Conn"),
                propertiesFile.getString("DBUser"), propertiesFile.getString("DBPass")));
        }
    }
    /**
     * gets connection
     * @return used connections -1
     */
    public static synchronized Connection getConnection(){
        while(true){
            if(freeConnections.size() > 0){
                Connection connection = freeConnections.get(freeConnections.size()-1);
                freeConnections.remove(freeConnections.size()-1);
                usedConnections.add(connection);
                break;
            }
        }
        return usedConnections.get(usedConnections.size()-1);
    }
    /**
     * adds connection to freeConnections and removes recieved connection
     * @param connection 
     */
    public static synchronized void releaseConnection(Connection connection){
        freeConnections.add(connection);
        usedConnections.remove(connection);
    }
    
}
