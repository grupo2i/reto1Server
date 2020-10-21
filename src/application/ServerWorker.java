/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
    
    private void HandleClientMessages(Message clientMessage) throws IOException, ClassNotFoundException {
        //Get message type
        Message.Type messageType = clientMessage.getType();
        System.out.println("Message type: " + messageType);
        switch(messageType) {
            case SIGN_UP:
                User signUpUser = (User)clientMessage.getData();
                signUpUser.printData();
                break;
            case SIGN_IN:
                User logInUser = (User)clientMessage.getData();
                System.out.println("Login: " + logInUser.getLogin());
                System.out.println("Password: " + logInUser.getPassword());
                break;
            case LOG_OFF:
                break;
            case CLOSE_CONNECTION:
                break;
            default:
                break;
        }
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
                
                //Send a response
                if(input.getType() != Message.Type.CLOSE_CONNECTION) {
                    serverOutput.writeObject(input);
                    serverOutput.flush();
                }
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
