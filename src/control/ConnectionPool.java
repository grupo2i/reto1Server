package control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Provides connections when asked to.
 * @author Aitor Fidalgo
 */
public class ConnectionPool {
    private static Integer totalDBConnections;
    
    private static ArrayList<Connection> freeConnections = new ArrayList<>();
    private static ArrayList<Connection> usedConnections = new ArrayList<>();
    
    private static ResourceBundle propertiesFile = null;
    
    /**
     * Initializes the pool to congif file
     * @throws SQLException If something goes wrong.
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
     * Keeps looping until theres a free connection and returns it.
     * @return A free connection wich is now a used connection.
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
     * Adds a used connection to free connections.
     * @param connection The connection that is being freed.
     */
    public static synchronized void releaseConnection(Connection connection){
        freeConnections.add(connection);
        usedConnections.remove(connection);
    }
    
}
