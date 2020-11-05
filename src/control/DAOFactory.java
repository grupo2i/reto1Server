package control;

/**
 * generates DAO's when asked
 * @author Ander
 */
public class DAOFactory {
    
    public static synchronized DAO getDao(int opc){
        switch(opc){
            case 1:
                return new DAOImplementation();
            default:
                return null;
        }
    }
}
