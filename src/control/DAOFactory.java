/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

/**
 *
 * @author Ander
 */
public class DAOFactory {
    
    public DAO getDao(int opc){
        switch(opc){
            case 1:
                return new DAOImplementation();
            default:
                return null;
        }
    }
}
