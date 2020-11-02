package control;

import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UserAlreadyExistsException;
import user.User;
import exceptions.UserNotFoundException;
import java.io.IOException;

/**
 *
 * @author aitor
 */
public interface DAO {
    public void Disconnect();
    public User getUserByUsername(String username) throws UserNotFoundException, IOException;
    public User signIn(User user) throws UserNotFoundException, PasswordDoesNotMatchException;
    public User signUp(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException;
    public boolean userNameIsRegistered(String username);
    public boolean emailIsRegistered(String email);
    
    
}