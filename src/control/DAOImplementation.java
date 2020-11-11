package control;

import exceptions.PasswordDoesNotMatchException;
import exceptions.UserNotFoundException;
import exceptions.UserAlreadyExistsException;
import exceptions.EmailAlreadyExistsException;
import exceptions.UnexpectedErrorException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import user.User;

/**
 * Data Access Object.
 *
 * @see ConnectionPool
 * @author Ander Vicente
 */
public class DAOImplementation implements DAO {

    private Statement stmt = null;
    private ResultSet rs = null;
    private Connection conn = null;

    /**
     * Gets a free connection from de ConnectionPool.
     *
     * @see ConnectionPool
     */
    public DAOImplementation() {
        conn = ConnectionPool.getConnection();
    }

    /**
     * Closes ResultSet, Statement and releases the connection to the
     * ConnectionPool.
     *
     * @throws SQLException If something goes wrong.
     * @see ConnectionPool
     */
    @Override
    public void Disconnect() throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            ConnectionPool.releaseConnection(conn);
        }
    }

    /**
     * Returns a full User instance from the DB.
     *
     * @param username Used to look for the user.
     * @return A user with all the data registered in the DB.
     * @throws UserNotFoundException If the user is not registered in the DB.
     * @throws IOException If something goes wrong.
     * @throws SQLException If something goes wrong.
     */
    @Override
    public User getUserByUsername(String username) throws UserNotFoundException, IOException, SQLException {
        User auxUser = new User();
        /* stmt = conn.createStatement();
        String query;
        query = "SELECT * FROM user WHERE username like '" + username + "';";
        stmt.execute(query);
        rs = stmt.getResultSet();*/
        
       /* String query = "SELECT * FROM user WHERE username like '" + username + "';";
        PreparedStatement st;
        st = conn.prepareStatement(query);
        rs = st.executeQuery();*/
        
        String query = "SELECT * FROM user WHERE username like '" + username + "';";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.execute();
        rs = statement.executeQuery();

        if (rs.next()) {
            auxUser.setId(rs.getInt("id"));
            auxUser.setLogin(rs.getString("username"));
            auxUser.setEmail(rs.getString("email"));
            auxUser.setFullName(rs.getString("name"));
            auxUser.setPassword(rs.getString("password"));
            auxUser.setStatus(User.UserStatus.valueOf(rs.getString("status")));
            auxUser.setPrivilege(User.UserPrivilege.valueOf(rs.getString("privilege")));
            auxUser.setLastAccess(rs.getDate("lastAccess"));
            auxUser.setLastPasswordChange(rs.getDate("lastPasswordChange"));
        } else {
            throw new exceptions.UserNotFoundException(username);
        }
        return auxUser;
    }

    /**
     * Checks if there is any error in the signIn operation.
     *
     * @param user The user trying to sign in.
     * @return All the data of the user trying to sign in.
     * @throws UserNotFoundException If the user is not registered in the DB.
     * @throws PasswordDoesNotMatchException If the introduced password does not
     * match with the user.
     * @throws SQLException If something goes wrong.
     * @throws UnexpectedErrorException If something goes wrong.
     */
    @Override
    public User signIn(User user) throws UserNotFoundException, PasswordDoesNotMatchException, SQLException, UnexpectedErrorException {
        User auxUser = null;
        try {
            auxUser = getUserByUsername(user.getLogin());
            if (!auxUser.getPassword().equals(user.getPassword())) {
                throw new PasswordDoesNotMatchException();
            }
            auxUser.setLastAccess(Date.valueOf(LocalDate.now()));
            updateUserOnLogIn(auxUser.getLastAccess(), auxUser.getId());
        } catch (SQLException | IOException e) {
            throw new UnexpectedErrorException(e.getMessage());
        } finally {
            Disconnect();
        }
        return auxUser;
    }

    /**
     * Updates the last access of a user after loging in.
     *
     * @param lastAccess Date of the last access (now).
     * @param id Used to identify the user to update.
     * @throws SQLException If something goes wrong.
     */
    public void updateUserOnLogIn(Date lastAccess, Integer id) throws SQLException {
        /*stmt = conn.createStatement();
        String query;
        query = "UPDATE user SET lastAccess='" + lastAccess + "' WHERE id=" + id + ";";
        stmt.executeUpdate(query);*/
        /*
        String query = "UPDATE user SET lastAccess='" + lastAccess + "' WHERE id=" + id + ";";
        PreparedStatement st;
        st = conn.prepareStatement(query);
        rs = st.executeQuery();*/
        
        String query = "UPDATE user SET lastAccess='" + lastAccess + "' WHERE id=" + id + ";";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.execute();
        //rs = statement.executeQuery();
       /* statement.executeUpdate();
        statement.close();*/

    }

    /**
     * Registers a user in the DB.
     *
     * @param user User trying to register.
     * @return A user instance with the remaining data the client did not
     * specify.
     * @throws UserAlreadyExistsException If the username is already registered
     * in the DB.
     * @throws EmailAlreadyExistsException If the email is already registered in
     * the DB
     * @throws SQLException If something goes wrong.
     * @throws UnexpectedErrorException If something ges wrong.
     */
    @Override
    public User signUp(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException, SQLException, UnexpectedErrorException {
        try {
            Integer idAssign;

            if (userNameIsRegistered(user.getLogin())) {
                throw new UserAlreadyExistsException(user.getLogin());
            }
            if (emailIsRegistered(user.getEmail())) {
                throw new EmailAlreadyExistsException(user.getEmail());
            }

            /*stmt = conn.createStatement();
            String query;
            query = "SELECT COUNT(*) FROM user;";
            stmt.execute(query);
            rs = stmt.getResultSet();*/
            String query = "SELECT COUNT(*) FROM user;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute();
            rs = statement.executeQuery();

            if (rs.next()) {
                idAssign = rs.getInt(1);

            } else {
                idAssign = 0;
            }
            //Setting the data the client did not specify...
            user.setId(idAssign + 1);
            user.setStatus(User.UserStatus.ENABLED);
            user.setPrivilege(User.UserPrivilege.USER);
            user.setLastAccess(Date.valueOf(LocalDate.now()));
            user.setLastPasswordChange(Date.valueOf(LocalDate.now()));

            //Inserting the users data...
            String insert = "insert into user values(?,?,?,?,?,?,?,?,?);";
            PreparedStatement st;
            st = conn.prepareStatement(insert);
            st.setInt(1, user.getId());
            st.setString(2, user.getLogin());
            st.setString(3, user.getEmail());
            st.setString(4, user.getFullName());
            st.setString(5, user.getStatus().toString());
            st.setString(6, user.getPrivilege().toString());
            st.setString(7, user.getPassword());
            st.setDate(8, user.getLastAccess());
            st.setDate(9, user.getLastPasswordChange());
            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            throw new UnexpectedErrorException(e.getMessage());
        } finally {
            Disconnect();
        }
        return user;
    }

    /**
     * Checks if a username is already registered or not.
     *
     * @param username Username that is being checked.
     * @return True if it is already registered, false if not.
     * @throws SQLException If something goes wrong.
     */
    @Override
    public boolean userNameIsRegistered(String username) throws SQLException {
        boolean isRegistered = false;
        /* stmt = conn.createStatement();
        String query;
        query = "SELECT * FROM user WHERE username = '" + username + "';";
        stmt.execute(query);
        rs = stmt.getResultSet();
         */
        String query = "SELECT * FROM user WHERE username = '" + username + "';";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.execute();
        rs = statement.executeQuery();
        /*statement.executeUpdate();
        statement.close();*/
        if (rs.next()) {
            isRegistered = true;
        }
        return isRegistered;
    }

    /**
     * Checks if an email is already registered or not.
     *
     * @param email Email that is being checked.
     * @return True if it is already registered, false if not.
     * @throws SQLException If something goes wrong.
     */
    @Override
    public boolean emailIsRegistered(String email) throws SQLException {
        boolean esta = false;
        /* stmt = conn.createStatement();
        String query;
        query = "SELECT * FROM user WHERE email = '" + email + "';";
        stmt.execute(query);
        rs = stmt.getResultSet();*/
        String query = "SELECT * FROM user WHERE email = '" + email + "';";
        PreparedStatement statement = conn.prepareStatement(query);
        rs = statement.executeQuery();
        if (rs.next()) {
            esta = true;
        }
        return esta;
    }
    
   /* private static java.sql.Timestamp getCurrentTimeStamp() {

    java.util.Date today = new java.util.Date();
    return new java.sql.Timestamp(today.getTime());

}*/
/*
    @Override
    public void updateUserOnLogIn(Date lastAccess, Integer id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
}

