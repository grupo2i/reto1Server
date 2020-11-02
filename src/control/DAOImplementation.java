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
    

    public DAOImplementation(){
        conn = ConnectionPool.getConnection();
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
                ConnectionPool.releaseConnection(conn);
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
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE username like '" + username + "';";
            stmt.execute(query);
            rs = stmt.getResultSet();

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
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            
        }
        return auxUser;
    }

    /**
     * 
     * @param user
     * @return
     * @throws UserNotFoundException
     * @throws PasswordDoesNotMatchException
     */
    @Override
    public User signIn(User user) throws UserNotFoundException, PasswordDoesNotMatchException {
        User auxUser = null;
        try {
            auxUser = getUserByUsername(user.getLogin());
            if (!auxUser.getPassword().equals(user.getPassword())) {
                throw new PasswordDoesNotMatchException();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(DAOImplementation.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            Disconnect();
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
            
        }
        return esta;
    }

}