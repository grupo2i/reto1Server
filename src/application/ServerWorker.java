/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import dummy.DataTest;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import message.Message;

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
     * Handles messages from the clientSocket.
     */
    @Override
    public void run() {
        System.out.println("Client connected.");
        try {
            Message input = new Message(Message.Type.LOG_OFF, null); //Dummy message
            while(input.getType() != Message.Type.CLOSE_CONNECTION) {
                input = (Message)clientInput.readObject();
                if(input.getType() != Message.Type.CLOSE_CONNECTION) {
                    System.out.print("Message type: " + input.getType());
                    DataTest data = (DataTest)input.getData();
                    System.out.print(" number: " + data.getNum());
                    System.out.println(" message: " + data.getMsg());
                    serverOutput.writeObject(input);
                    serverOutput.flush();
                } else {
                    System.out.println("Message type: " + input.getType());
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
