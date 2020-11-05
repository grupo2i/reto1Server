package control;

import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UnexpectedErrorException;
import exceptions.UserAlreadyExistsException;
import user.User;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

/**
 * DAO's interface
 * @author Ander Vicente
 */
public interface DAO {
    /**
     * Closes ResultSet, Statement and releases the connection to the ConnectionPool.
     * @throws SQLException If something goes wrong.
     */
    public void Disconnect() throws SQLException;
    /**
     * Returns a full User instance from the DB.
     * @param username Used to look for the user.
     * @return A user with all the data registered in the DB.
     * @throws UserNotFoundException If the user is not registered in the DB.
     * @throws IOException If something goes wrong.
     * @throws SQLException If something goes wrong.
     */
    public User getUserByUsername(String username) throws UserNotFoundException, IOException, SQLException;
    /**
     * Checks if there is any error in the signIn operation.
     * @param user The user trying to sign in.
     * @return All the data of the user trying to sign in.
     * @throws UserNotFoundException If the user is not registered in the DB.
     * @throws PasswordDoesNotMatchException If the introduced password does not match with the user.
     * @throws SQLException If something goes wrong.
     * @throws UnexpectedErrorException If something goes wrong.
     */
    public User signIn(User user) throws UserNotFoundException, PasswordDoesNotMatchException,
            UnexpectedErrorException, SQLException;
    /**
     * Updates the last access of a user after loging in.
     * @param lastAccess Date of the last access (now).
     * @param id Used to identify the user to update.
     * @throws SQLException If something goes wrong.
     */
    public void updateUserOnLogIn(Date lastAccess, Integer id) throws SQLException;
    /**
     * Registers a user in the DB.
     * @param user User trying to register.
     * @return A user instance with the remaining data the client did not specify.
     * @throws UserAlreadyExistsException If the username is already registered in the DB.
     * @throws EmailAlreadyExistsException If the email is already registered in the DB
     * @throws SQLException If something goes wrong.
     * @throws UnexpectedErrorException If something ges wrong.
     */
    public User signUp(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException,
            UnexpectedErrorException, SQLException;
    /**
     * Checks if a username is already registered or not.
     * @param username Username that is being checked.
     * @return True if it is already registered, false if not.
     * @throws SQLException If something goes wrong.
     */
    public boolean userNameIsRegistered(String username) throws SQLException;
    /**
     * Checks if an email is already registered or not.
     * @param email Email that is being checked.
     * @return True if it is already registered, false if not.
     * @throws SQLException If something goes wrong.
     */
    public boolean emailIsRegistered(String email) throws SQLException;
    
    
}