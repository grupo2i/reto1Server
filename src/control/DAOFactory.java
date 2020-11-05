package control;

/**
 * Generates DAOs when asked to.
 * @author Ander Vicente
 */
public class DAOFactory {
    /**
     * Return a DAO implementation.
     * @return A DAO implementation.
     */
    public static synchronized DAO getDao(){
        return new DAOImplementation();
    }
}
