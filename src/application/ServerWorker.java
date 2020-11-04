package application;

import control.DAO;
import control.DAOFactory;
import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UnexpectedErrorException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.UnexpectedException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import user.User;

/**
 * Handles messages from a client. Every accepted connection launches a ServerWorker
 * @author Martin Angulo
 */
public class ServerWorker extends Thread {
    private Socket clientSocket = null;
    private ObjectInputStream clientInput = null;
    private ObjectOutputStream serverOutput = null;
    private Boolean hasConnection;
    
    /**
     * ServerWorker constructor, initializes IO with the client.
     * @param client Client socket from the accepted connection.
     * @param hasConnection
     */
    public ServerWorker(Socket client, Boolean hasConnection) {
        try {
            this.hasConnection = hasConnection;
            if(this.hasConnection) ServerApplication.useClientConnection();
            //Should check the socket
            clientSocket = client;
            //The order is important! (Opposite of clients order to avoid a deadlock)
            serverOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            clientInput = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, "IOException: {0}", ex.getMessage());
        }
    }
    
    /**
     * Method to handle messages from the client.
     * @param clientMessage Message to handle.
     */
    private void HandleClientMessages(Message clientMessage) throws IOException, ClassNotFoundException {
        //Get message type
        Message.Type messageType = clientMessage.getType();
        Message returnMessage;
        DAO dao = DAOFactory.getDao(1);
        User user = (User)clientMessage.getData();
        switch(messageType) {
            case SIGN_UP:
                try{
                    user = dao.signUp(user);
                    //The following line wont be executed if there is any of the catched exceptions.
                    returnMessage = new Message(Message.Type.SIGN_UP, user);
                }catch(UserAlreadyExistsException e){
                    returnMessage = new Message(Message.Type.USER_ALREADY_EXISTS, user);
                }catch(EmailAlreadyExistsException e){
                    returnMessage = new Message(Message.Type.EMAIL_ALREADY_EXISTS, user);
                } catch (SQLException | UnexpectedErrorException ex) {
                    returnMessage = new Message(Message.Type.UNEXPECTED_ERROR, user);
                }
                break;
            case SIGN_IN:
                try{
                    user = dao.signIn(user);
                    //The following line wont be executed if there is any of the catched exceptions.
                    returnMessage = new Message(Message.Type.SIGN_IN, user);
                }catch(UserNotFoundException e){
                    returnMessage = new Message(Message.Type.USER_NOT_FOUND, user);
                }catch(PasswordDoesNotMatchException e){
                    returnMessage = new Message(Message.Type.PASSWORD_DOES_NOT_MATCH, user);
                }catch(SQLException | UnexpectedErrorException e){
                    returnMessage = new Message(Message.Type.UNEXPECTED_ERROR, new User());
                }
                break;
            default:
                throw new UnexpectedException("Message recieve from the client was not valid.");
        }
        serverOutput.writeObject(returnMessage);
        serverOutput.flush();
    }
    
    /**
     * Handles messages from the clientSocket.
     */
    @Override
    public void run() {
        try {
            if(hasConnection){
                //Read and handle client messages
                Message input = (Message)clientInput.readObject();
                HandleClientMessages(input);
            }else{
                throw new UnexpectedErrorException("No connections avaliabe in the server, rejecting client.");
            }
        } catch (IOException | NullPointerException | ClassNotFoundException | UnexpectedErrorException e) {
            if(!(e instanceof UnexpectedErrorException)){
                Logger.getLogger(UnexpectedErrorException.class.getName()).log(Level.SEVERE, e.getMessage());
            }
            sendUnexpectedErrorMessage();
        } finally {
            try {
                disconnect();
            } catch(IOException ex) {
                Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, "IOException: {0}", ex.getMessage());
            }
        }
    }
    
    private void sendUnexpectedErrorMessage(){
        try {
            Message rejectMessage = new Message(Message.Type.UNEXPECTED_ERROR, new User());
            serverOutput.writeObject(rejectMessage);
            serverOutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void disconnect() throws IOException{
        Logger.getLogger(ServerWorker.class.getName()).log(Level.INFO, "Connection Closing.");
        if(serverOutput != null) {
           serverOutput.close();
           Logger.getLogger(ServerWorker.class.getName()).log(Level.INFO, "Socket Out Closed.");
        }
        if (clientInput != null) {
            clientInput.close(); 
            Logger.getLogger(ServerWorker.class.getName()).log(Level.INFO, "Socket Input Closed.");
        }
        if (clientSocket != null) {
            clientSocket.close();
            Logger.getLogger(ServerWorker.class.getName()).log(Level.INFO, "Socket Closed.");
        }
        if(hasConnection) ServerApplication.releaseClientConnection();
    }
}
