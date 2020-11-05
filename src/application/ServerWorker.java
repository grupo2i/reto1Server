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
 * @author Martin Angulo, Aitor Fidalgo
 */
public class ServerWorker extends Thread {
    private Socket clientSocket = null;
    private ObjectInputStream clientInput = null;
    private ObjectOutputStream serverOutput = null;
    private Boolean hasConnection;
    
    /**
     * ServerWorker constructor, initializes IO with the client.
     * @param client Client socket from the accepted connection.
     * @param hasConnection True if the worker should provide response
     * false if it should reject the client. 
     */
    public ServerWorker(Socket client, Boolean hasConnection) {
        try {
            this.hasConnection = hasConnection;
            //Decrementing free connection if the worker is using one.
            if(this.hasConnection) ServerApplication.useClientConnection();
            clientSocket = client;
            serverOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            clientInput = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, "IOException: {0}", ex.getMessage());
        }
    }
    
    /**
     * Method to handle messages from the client.
     * @param clientMessage Message to handle.
     * @throws IOException If something goes wrong.
     * @throws ClassNotFoundException If soething goes wrong.
     */
    private void HandleClientMessages(Message clientMessage) throws IOException, ClassNotFoundException {
        //Getting message type.
        Message.Type messageType = clientMessage.getType();
        Message returnMessage;
        DAO dao = DAOFactory.getDao();
        //Getting User from the recieved message.
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
        //Sending response.
        serverOutput.writeObject(returnMessage);
        serverOutput.flush();
    }
    
    /**
     * Handles messages from clientSocket or rejects them by sending error message.
     */
    @Override
    public void run() {
        try {
            if(hasConnection){
                //Read and handle client messages
                Message input = (Message)clientInput.readObject();
                HandleClientMessages(input);
            }else{
                //Reject clients connection.
                throw new UnexpectedErrorException("No connections avaliabe in the server, rejecting client.");
            }
        } catch (IOException | NullPointerException | ClassNotFoundException | UnexpectedErrorException e) {
            //UnexpectedErrorException shows its own logger message.
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
    
    /**
     * Sends an unexpected error message to the client.
     */
    private void sendUnexpectedErrorMessage(){
        try {
            Message rejectMessage = new Message(Message.Type.UNEXPECTED_ERROR, new User());
            serverOutput.writeObject(rejectMessage);
            serverOutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Closes IO and Socket objects and releases a connection if its being used one.
     * @throws IOException If something goes wrong.
     */
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
