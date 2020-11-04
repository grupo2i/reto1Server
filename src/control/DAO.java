package control;

import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UnexpectedErrorException;
import exceptions.UserAlreadyExistsException;
import user.User;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author aitor
 */
public interface DAO {
    public void Disconnect() throws SQLException;
    public User getUserByUsername(String username) throws UserNotFoundException, IOException, SQLException;
    public User signIn(User user) throws UserNotFoundException, PasswordDoesNotMatchException,
            UnexpectedErrorException, SQLException;
    public User signUp(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException,
            UnexpectedErrorException, SQLException;
    public boolean userNameIsRegistered(String username) throws SQLException;
    public boolean emailIsRegistered(String email) throws SQLException;
    
    
}