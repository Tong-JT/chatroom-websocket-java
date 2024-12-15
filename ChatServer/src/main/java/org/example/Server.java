package org.example;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private Map<String, Chatroom> chatrooms;
    private Map<String, ClientThread> clientsOnline;

    public Server() {
        chatrooms = new HashMap<>();
        clientsOnline = new HashMap<>();
        defaultChatrooms();
    }

    public void startServer() throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        try {
            while (true) {
                System.out.println("Waiting for client to connect...");
                Socket clientSocket = serverSocket.accept();
                ClientThread newClient = new ClientThread(clientSocket, this);
                newClient.start();
                System.out.println("New client connected.");
            }
        } finally {
            serverSocket.close();
        }
    }

    public void defaultChatrooms() {
        chatrooms.put("Blizzard", new Chatroom("Blizzard"));
        chatrooms.put("Sleet", new Chatroom("Sleet"));
        chatrooms.put("Snowball", new Chatroom("Snowball"));
        chatrooms.put("Avalanche", new Chatroom("Avalanche"));
    }

    public void createChatroom(String chatroomName) {
        if (!chatrooms.containsKey(chatroomName)) {
            chatrooms.put(chatroomName, new Chatroom(chatroomName));
            System.out.println("Created new chatroom: " + chatroomName);
        }
    }

    public void addClientToChatroom(ClientThread client, String chatroomName) {
        if (chatrooms.containsKey(chatroomName)) {
            chatrooms.get(chatroomName).addClient(client);
            client.sendMessageToClient("ChatroomJoined" + chatroomName);
            System.out.println(client + " added to " + chatroomName);
            broadcastMessage(client + " has joined " + chatroomName, client);
        } else {
            System.out.println("Chatroom not found: " + chatroomName);
        }
    }

    public String listChatrooms() {
        List<Map<String, Object>> chatroomsList = new ArrayList<>();
        for (Chatroom chatroom : chatrooms.values()) {
            Map<String, Object> chatroomData = new HashMap<>();
            chatroomData.put("name", chatroom.getName());
            chatroomData.put("clientCount", chatroom.getUserNum());
            chatroomsList.add(chatroomData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("chatrooms", chatroomsList);

        Gson gson = new Gson();
        return gson.toJson(response);
    }

    public Map<String, ClientThread> getClientsOnline(){
        return clientsOnline;
    }

    public void broadcastMessage(String message, ClientThread client) {
        Chatroom chatroom = findChatroomFromClient(client);
        chatroom.broadcastMessage(message);
    }

    public Chatroom findChatroomFromClient(ClientThread client) {
        for (Chatroom chatroom : chatrooms.values()) {
            if (chatroom.getClients().contains(client)) {
                return chatroom;
            }
        }
        return null;
    }
}
