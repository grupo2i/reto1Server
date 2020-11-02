/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import control.DAO;
import control.DAOFactory;
import control.DAOImplementation;
import exceptions.EmailAlreadyExistsException;
import exceptions.PasswordDoesNotMatchException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
    private ObjectOutputStream serverOutput =null;
    
    /**
     * ServerWorker constructor, initializes IO with the client.
     * @param client Client socket from the accepted connection.
     */
    public ServerWorker(Socket client) {
        //Should check the socket
        clientSocket = client;
        try {
            //The order is important! (Opposite of clients order to avoid a deadlock)
            serverOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            clientInput = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println("IO Error/ ServerWorker terminated abruptly");
        }
    }
    
    /**
     * Method to handle messages from the client.
     * @param clientMessage Message to handle.
     */
    private void HandleClientMessages(Message clientMessage) throws IOException, ClassNotFoundException {
        //Get message type
        Message.Type messageType = clientMessage.getType();
        Message returnMessage = null;
        DAO dao = DAOFactory.getDao(1);
        switch(messageType) {
            case SIGN_UP:
                User user = (User)clientMessage.getData();
                try{
                    user = dao.signUp(user);
                    //The following line wont be executed if there is any of the catched exceptions.
                    returnMessage = new Message(Message.Type.SIGN_UP, user);
                }catch(UserAlreadyExistsException e){
                    returnMessage = new Message(Message.Type.USER_ALREADY_EXISTS, user);
                }catch(EmailAlreadyExistsException e){
                    returnMessage = new Message(Message.Type.EMAIL_ALREADY_EXISTS, user);
                }
                break;
            case SIGN_IN:
                User signInUser = (User)clientMessage.getData();
                try{
                    signInUser = dao.signIn(signInUser);
                    //The following line wont be executed if there is any of the catched exceptions.
                    returnMessage = new Message(Message.Type.SIGN_IN, signInUser);
                }catch(UserNotFoundException e){
                    returnMessage = new Message(Message.Type.USER_NOT_FOUND, signInUser);
                }catch(PasswordDoesNotMatchException e){
                    returnMessage = new Message(Message.Type.PASSWORD_DOES_NOT_MATCH, signInUser);
                }
                break;
            case LOG_OFF:
                break;
            case CLOSE_CONNECTION:
                break;
            default:
                break;
        }
        serverOutput.writeObject(returnMessage);
        serverOutput.flush();
    }
    
    /**
     * Handles messages from the clientSocket.
     */
    @Override
    public void run() {
        System.out.println("Client connected.");
        try {
            Message input = new Message(Message.Type.LOG_OFF, null); //Dummy message
            while(input.getType() != Message.Type.CLOSE_CONNECTION) {
                //Read and handle client messages
                input = (Message)clientInput.readObject();
                HandleClientMessages(input);
            }
        } catch (IOException e) {
            String worker = this.getClass().getName();
            System.out.println("IO Error/ Client " + worker + " terminated abruptly");
            System.out.println("Error msg " + e.getMessage());
        }
        catch(NullPointerException e){
            String worker = this.getClass().getName(); //reused String line for getting thread name
            System.out.println("Client "+ worker +" Closed");
        } catch (ClassNotFoundException ex) {
            String worker = this.getClass().getName(); //reused String line for getting thread name
            System.out.println("ClassNotFoundException "+ worker +" terminated abruptly");
            System.out.println("Error msg " + ex.getMessage());
        } finally {    
            try {
                System.out.println("Connection Closing..");
                if(serverOutput != null) {
                   serverOutput.close();
                   System.out.println("Socket Out Closed");
                }
                if (clientInput != null) {
                    clientInput.close(); 
                    System.out.println("Socket Input Closed");
                }
                if (clientSocket != null) {
                    clientSocket.close();
                    System.out.println("Socket Closed");
                }
            } catch(IOException ie) {
                System.out.println("Socket Close Error");
            }
        }
    }
}
