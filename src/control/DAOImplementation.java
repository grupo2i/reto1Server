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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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
        return ConnectionPool2.getConnection();
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
                //conn.close();
                ConnectionPool2.releaseConnection(conn);
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
     * @throws UserAlreadyExistsException
     * @throws EmailAlreadyExistsException
     */
    @Override
    public User signUp(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException {
        try {
            Integer idAssign;

            if (userNameIsRegistered(user.getLogin())) {
                throw new UserAlreadyExistsException(user.getLogin());
            }
            if (emailIsRegistered(user.getEmail())) {
                throw new EmailAlreadyExistsException(user.getEmail());
            }
            conn = Connect();
            stmt = conn.createStatement();
            stmt = conn.createStatement();
            String query;
            query = "SELECT COUNT(*) FROM user;";
            stmt.execute(query);
            rs = stmt.getResultSet();
            if(rs.next()){
                idAssign = rs.getInt(1);
                
            }else{
                idAssign = 0;
            }
            user.setId(idAssign + 1);
            user.setStatus(User.UserStatus.ENABLED);
            user.setPrivilege(User.UserPrivilege.USER);
            user.setLastAccess(Date.valueOf(LocalDate.now()));
            user.setLastPasswordChange(Date.valueOf(LocalDate.now()));

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

        } catch (SQLException ex) {
            Logger.getLogger(DAOImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Disconnect();
        }
        return user;
    }

    /**
     * 
     * @param username
     * @return 
     */
    @Override
    public boolean userNameIsRegistered(String username) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE username = '" + username + "';";
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
     * @param email
     * @return
     */
    @Override
    public boolean emailIsRegistered(String email) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE email = '" + email + "';";
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
