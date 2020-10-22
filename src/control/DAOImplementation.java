/**
 * 
 */
package control;

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
 * @author Ander
 */
public class DAOImplementation implements DAO{

    private Statement stmt = null;
    private ResultSet rs = null;
    private Connection conn = null;

    /**
     * Connects to the database.
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
            if(rs != null)
                rs.close();        
            if(stmt != null)
                stmt.close();            
            if(conn != null)
                conn.close();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Disconnect.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    
    /**
     * Retrieves user data for the specified username.
     * @param username
     * @return true/false
     */
    @Override
    public boolean checkUsername(String username) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE username like " + username +";";
            stmt.execute(query);
            rs = stmt.getResultSet();
        if(rs.next())
                esta = true;
            Disconnect();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return esta;
    }
    /**
     * Retrieves user data for the specified password.
     * @param password
     * @return password
     */
    @Override
    public boolean checkPassword(String password) {
        boolean esta = false;
        try {
            conn = Connect();
            stmt = conn.createStatement();
            String query;
            query = "SELECT * FROM user WHERE password like " + password +";";
            stmt.execute(query);
            rs = stmt.getResultSet();
        if(rs.next())
                esta = true;
            Disconnect();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("ERROR: Consult error.");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return esta;
    }
    public void signUp(User user)throws SQLException{
         boolean ok = true;
         Integer idAssign = null;
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
            if(rs.next()){
              idAssign = rs.getInt(0);
            }
            
        String insert = "insert into user(id,username,email,name,status,privilege,password,lastAccess,lastPasswordChange)";
            
            try {
           // int result = stmt.executeUpdate(insert);
            PreparedStatement st;
            st = conn.prepareStatement(insert);
            st.setInt(1, idAssign+1);
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
        } 
        Disconnect();
    }
        
        
    }

