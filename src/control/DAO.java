package control;

import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UserAlreadyExistsException;
import java.sql.Connection;
import user.User;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author aitor
 */
public interface DAO {
    public Connection Connect();
    public void Disconnect();
    public User getUserByUsername(String username) throws UserNotFoundException, IOException;
    public User signIn(User user) throws UserNotFoundException, IOException, PasswordDoesNotMatchException;
    public User signUp(User user) throws SQLException, UserAlreadyExistsException, EmailAlreadyExistsException;
    public boolean userNameIsRegistered(String username);
    public boolean emailIsRegistered(String email);
    
    
}