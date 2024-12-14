package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public class ClientSocket {

    private Socket clientSocket;
    private UserInterface userInterface;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isInChatroom = false;
    private Thread listenerThread;
    private String encryptionKey;

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
                        Platform.runLater(() -> {
                            userInterface.getChatSide().updateChatroomList(chatrooms);
                        });


                    } else if (message.contains("ChatroomJoined")) {
                        String chatroomName = message.substring("ChatroomJoined".length()).trim();
                        userInterface.enterChatroom(chatroomName);
                        isInChatroom = true;
                        System.out.println("Joined chatroom: " + chatroomName);

                    } else if (message.contains("ChatMessage")) {
                        String chatMessage = message.substring("ChatMessage".length()).trim();
                        System.out.println("Chat message received: " + chatMessage);
                        userInterface.getChatroomSide().printMessage(chatMessage);

                    } else if (message.contains("ChatroomCreated")) {
                        String chatroomName = message.substring("ChatroomCreated".length()).trim();
                        sendMessage("JoinChatroom" + chatroomName);
                        receiveMessage();

                    } else if (message.contains("NewSymmetricKey")) {
                        System.out.println(message);
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

    public void startListeningForMessages() {
        listenerThread = new Thread(() -> {
            while (isInChatroom && clientSocket != null && !clientSocket.isClosed()) {
                receiveMessage();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void stopListeningForMessages() {
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
            System.out.println("Stopped listening for messages.");
        }
    }

    public void setIsInChatroom(boolean isInChatroom) {
        this.isInChatroom = isInChatroom;
    }

    public String receivePublicKey() {
        try {
            String message = in.readLine();
            if (message != null && message.startsWith("PublicKey")) {
                userInterface.getEncryptionSide().toggleEncryption(true);
                return message.substring("PublicKey".length()).trim();
            }
        } catch (IOException e) {
            userInterface.getEncryptionSide().toggleEncryption(false);
            System.err.println("Error receiving public key: " + e.getMessage());
        }
        return null;
    }

}

