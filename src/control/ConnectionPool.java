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
 *
 * @author aitor
 */
public class ConnectionPool {
    private static final Integer TOTAL_CONNECTIONS = 10;
    
    private static ArrayList<Connection> freeConnections = new ArrayList<>();
    private static ArrayList<Connection> usedConnections = new ArrayList<>();
    
    private static ResourceBundle propertiesFile = null;
    
    
    public static void initializePool(){
        try{
            propertiesFile = ResourceBundle.getBundle("configuration.config");
            for(int i = 0; i < TOTAL_CONNECTIONS; i++){
                freeConnections.add(DriverManager.getConnection(propertiesFile.getString("Conn"),
                    propertiesFile.getString("DBUser"), propertiesFile.getString("DBPass")));
            }
        }catch(SQLException ex){
            System.out.println(ex + ": InitializePool()");
        }
    }
    
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
    
    public static synchronized void releaseConnection(Connection connection){
        freeConnections.add(connection);
        usedConnections.remove(connection);
    }
    
}
