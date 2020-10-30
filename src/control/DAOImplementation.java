/**
 *
 */
package control;

import exceptions.PasswordDoesNotMatchException;
import exceptions.UserNotFoundException;
import exceptions.UserAlreadyExistsException;
import exceptions.EmailAlreadyExistsException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import user.User;

/**
 * Data Access Object.
 *
 * @author Ander
 */
public class DAOImplementation implements DAO {

    private Statement stmt = null;
    private ResultSet rs = null;
    private Connection conn = null;

    /**
     * Connects to the database.
     *
     * @return Connection
     */
    @Override
    public Connection Connect() {
        try {
            return ConnectionPool.getConnection();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Connect.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        }
    }

    /**
     * Disconnects from the database.
     */
    @Override
    public void Disconnect() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Disconnect.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    /**
     * Gets user object recieving a username.
     *
     * @param username
     * @return true/false
     * @throws exceptions.UserNotFoundException
     * @throws java.io.IOException
     */
    @Override
    public User getUserByUsername(String username) throws UserNotFoundException, IOException {
        User auxUser = new User();
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE username like " + username + ";";
            stmt.execute(query);
            rs = stmt.getResultSet();

            if (rs.next()) {
                auxUser.setId(rs.getInt("id"));
                auxUser.setLogin(rs.getString("username"));
                auxUser.setEmail(rs.getString("email"));
                auxUser.setFullName(rs.getString("name"));
                auxUser.setPassword(rs.getString("password"));
                /*   char[] array = new char[8];
               rs.getCharacterStream(5).read(array);
               String aux = Char
                 */
                auxUser.setLastAccess(rs.getDate("lastAccess"));
                auxUser.setLastPasswordChange(rs.getDate("lastPasswordChange"));
            } else {
                throw new exceptions.UserNotFoundException(username);
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            Disconnect();
        }
        return auxUser;
    }

    /**
     * 
     * @param user
     * @return
     * @throws UserNotFoundException
     * @throws IOException
     * @throws PasswordDoesNotMatchException
     */
    @Override
    public User signIn(User user) throws UserNotFoundException, IOException, PasswordDoesNotMatchException {
        User auxUser = getUserByUsername(user.getLogin());
        if (!auxUser.getPassword().equals(user.getPassword())) {
            throw new PasswordDoesNotMatchException();
        }
        return auxUser;

    }
     /**
     *
     * @param user
     * @return
     * @throws SQLException
     * @throws UserAlreadyExistsException
     * @throws EmailAlreadyExistsException
     */
    @Override
    public User signUp(User user) throws SQLException, UserAlreadyExistsException, EmailAlreadyExistsException {
        User auxUser = new User();
        Integer idAssign = null;
        boolean estaUser = false;
        boolean estaEmail = false;

        if (userNameIsRegistered(user)) {
            throw new UserAlreadyExistsException(user);
        }
        if (emailIsRegistered(user)) {
            throw new EmailAlreadyExistsException(user);
        }
        conn = Connect();
        try {
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        stmt = conn.createStatement();
        String query;
        query = "SELECT COUNT (*) FROM user;";
        stmt.execute(query);
        rs = stmt.getResultSet();
        rs.next();
        idAssign = rs.getInt(0);

        String insert = "insert into user(id,username,email,name,status,privilege,password,lastAccess,lastPasswordChange)";

        try {
            PreparedStatement st;
            st = conn.prepareStatement(insert);
            st.setInt(1, idAssign + 1);
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
            auxUser = user;

        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        Disconnect();

        return auxUser;
    }

    @Override
    public boolean userNameIsRegistered(User user) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE username like " + user.getLogin() + ";";
            stmt.execute(query);
            rs = stmt.getResultSet();

            if (rs.next()) {
                esta = true;
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            Disconnect();
        }
        return esta;
    }

    /**
     *
     * @param user
     * @return
     */
    @Override
    public boolean emailIsRegistered(User user) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE email like " + user.getEmail() + ";";
            stmt.execute(query);
            rs = stmt.getResultSet();

            if (rs.next()) {
                esta = true;
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            Disconnect();
        }
        return esta;
    }

}
