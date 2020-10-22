package control;

import java.sql.Connection;

/**
 *
 * @author aitor
 */
public interface DAO {
    public Connection Connect();
    public void Disconnect();
    public boolean checkUsername(String username);
    public boolean checkPassword(String password);
    
}
