package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClientSocket {

    private Socket clientSocket;
    private UserInterface userInterface;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;

    public ClientSocket(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public boolean connect(String serverAddress, int port) {
        try {
            clientSocket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("Connected to the server!");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (clientSocket != null && !clientSocket.isClosed()) {
            out.println(message);
        }
    }

    public void receiveMessage() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                String message = in.readLine();
                if (message != null) {
                    System.out.println("Received from server: " + message);

                    if (message.contains("ListChatrooms")) {
                        String jsonData = message.substring("ListChatrooms".length()).trim();
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>(){}.getType();
                        Map<String, Object> response = gson.fromJson(jsonData, type);
                        List<Map<String, Object>> chatrooms = (List<Map<String, Object>>) response.get("chatrooms");
                        userInterface.getChatSide().updateChatroomList(chatrooms);

                    } else if (message.contains("ChatroomJoined")) {
                        String chatroomName = message.substring("ChatroomJoined".length()).trim();
                        userInterface.enterChatroom(chatroomName);
                        System.out.println("Joined chatroom: " + chatroomName);

                    } else if (message.contains("ChatMessage")) {
                        String chatMessage = message.substring("ChatMessage".length()).trim();
                        System.out.println("Chat message received: " + chatMessage);
                        userInterface.getChatroomSide().printMessage(chatMessage);

                    } else if (message.contains("ChatroomCreated")) {
                        String chatroomName = message.substring("ChatroomCreated".length()).trim();
                        sendMessage("JoinChatroom" + chatroomName);
                        receiveMessage();

                    }
                }
            } catch (IOException e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
    }

    public void close() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void listenForMessages() {
        listenerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.contains("ChatMessage")) {
                        String chatMessage = message.substring("ChatMessage".length()).trim();
                        System.out.println("Chat message received: " + chatMessage);
                        userInterface.getChatroomSide().printMessage(chatMessage);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
