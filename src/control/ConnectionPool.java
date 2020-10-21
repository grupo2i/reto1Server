/**
 * Contains all the classes related to data access.
 */
package control;

import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * A connection pool that provides connections to the DAO.
 * @see DAO
 * @author aitor
 */
public class ConnectionPool {
    private static ResourceBundle propertiesFile = null;
    private static BasicDataSource basicDataSource = null;
    
    /**
     * Initializes a BasicDataSource object and returns it.
     * @return BasicDataSouce object initialized.
     */
    public static BasicDataSource getBasicDataSource(){
        if(basicDataSource == null){
            basicDataSource = new BasicDataSource();

            //Object used to take data from 'properties' properties file.
            propertiesFile = ResourceBundle.getBundle("properties.properties");

            //Setting the DB attributes...
            basicDataSource.setDriverClassName(propertiesFile.getString("Driver"));
            basicDataSource.setUsername(propertiesFile.getString("DBUser"));
            basicDataSource.setPassword(propertiesFile.getString("DBPass"));
            basicDataSource.setUrl(propertiesFile.getString("Conn"));

            //Setting some connection pool attributes...
            basicDataSource.setInitialSize(10);
            basicDataSource.setMaxTotal(10);
            basicDataSource.setMaxWaitMillis(5000);
        }
        
        return basicDataSource;
    }
    
    /**
     * Return a connection to the DB if there is any avaliable.
     * @return A connection to the DB.
     * @throws java.sql.SQLException If something goes wrong.
     */
    public synchronized static Connection getConnection() throws SQLException {
        return (Connection) getBasicDataSource().getConnection();
    }
    
}
