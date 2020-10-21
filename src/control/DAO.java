package control;

import java.sql.Connection;
import user.User;

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
