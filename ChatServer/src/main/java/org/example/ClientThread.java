package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientThread(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {

                System.out.println("Received message from client: " + clientMessage);

                if (clientMessage.contains("Username")) {
                    username = clientMessage.substring("Username".length()).trim();
                    server.getClientsOnline().put(username, this);
                    System.out.println("Client connected with username: " + username);
                }

                else if (clientMessage.equalsIgnoreCase("ListChatrooms")) {
                    String chatroomsList = server.listChatrooms();
                    StringBuilder finalString = new StringBuilder("ListChatrooms"+chatroomsList);
                    out.println(finalString);

                } else if (clientMessage.contains("JoinChatroom")) {
                    String chatroomName = clientMessage.substring("JoinChatroom".length()).trim();
                    server.addClientToChatroom(this, chatroomName);

                }else if (clientMessage.equalsIgnoreCase("Disconnect")) {
                    System.out.println("Client requested to disconnect.");
                    break;

                }else if (clientMessage.contains("ChatMessage")) {
                    String message = clientMessage.substring("ChatMessage".length()).trim();
                    String newMessage = "ChatMessage[" + username + "] " + message;
                    server.broadcastMessage(newMessage, this);
                }else if (clientMessage.contains("CreateChatroom")) {
                    String chatroomName = clientMessage.substring("CreateChatroom".length()).trim();
                    server.createChatroom(chatroomName);
                    sendMessageToClient("ChatroomCreated" + chatroomName);
                }
            }

            handleClientDisconnection();

            System.out.println("Client disconnected or closed connection.");
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
            handleClientDisconnection();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Connection closed for " + clientSocket);
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }

    public void sendMessageToClient(String string) {
        out.println(string);
        System.out.println("Sending " + string);
    }

    private void handleClientDisconnection() {
        if (username != null) {
            server.getClientsOnline().remove(username);
        }

        Chatroom chatroom = server.findChatroomFromClient(this);
        if (chatroom != null) {
            chatroom.getClients().remove(this);
            chatroom.broadcastMessage("ChatMessage" + username + " has disconnected from " + chatroom.getName());
        }
    }

    public String toString() {
        return username;
    }
}
