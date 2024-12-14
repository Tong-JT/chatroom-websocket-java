package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Map;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String encrypt;
    private String key;
    private KeyPair keyPair;

    public ClientThread(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            keyPair = RSA.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Error generating key pair at server startup: " + e.getMessage());
        }
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

                } else if (clientMessage.equalsIgnoreCase("Disconnect")) {
                    System.out.println("Client requested to disconnect.");
                    break;

                } else if (clientMessage.contains("ChatMessage")) {
                    String message = clientMessage.substring("ChatMessage".length()).trim();
                    String decryptedMessage = decryptMessage(message);
                    String newMessage = "[" + username + "] " + decryptedMessage;
                    server.broadcastMessage(newMessage, this);

                } else if (clientMessage.contains("CreateChatroom")) {
                    String chatroomName = clientMessage.substring("CreateChatroom".length()).trim();
                    server.createChatroom(chatroomName);
                    sendMessageToClient("ChatroomCreated" + chatroomName);

                } else if (clientMessage.equalsIgnoreCase("RequestPublicKey")) {
                    try {
                        String publicKeyString = RSA.publicKeyToString(getKeyPair().getPublic());
                        sendMessageToClient("PublicKey" + publicKeyString);
                    } catch (Exception e) {
                        System.err.println("Error sending public key: " + e.getMessage());
                    }
                } else if (clientMessage.contains("NewSymmetricKey")) {
                    String encryptedKey = clientMessage.substring("NewSymmetricKey".length()).trim();
                    decryptSymmetricKey(encryptedKey);
                    sendMessageToClient("New key processed");
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

    public void sendChatToClient(String string) {
        String encryptedMessage = encryptMessage(string);
        JsonObject messageAndKey = new JsonObject();
        messageAndKey.addProperty("message", encryptedMessage);
        messageAndKey.addProperty("encryptedKey", encryptSymmetricKey());

        out.println("ChatMessage" + new Gson().toJson(messageAndKey));
        System.out.println("Sending encrypted message to client: " + new Gson().toJson(messageAndKey));
    }

    private String encryptMessage(String message) {
        String encryptedMessage = "";
        if (encrypt.contains("Caesar")) {
            CaesarCipher caesarCipher = new CaesarCipher();
            encryptedMessage = caesarCipher.encrypt(key, message);
        } else if (encrypt.contains("AES")) {
            AESCrypto aesCrypto = new AESCrypto();
            encryptedMessage = aesCrypto.encrypt(key, message);
        }
        return encryptedMessage;
    }

    private String encryptSymmetricKey() {
        try {
            JsonObject keyJson = new JsonObject();
            keyJson.addProperty("key", key);
            keyJson.addProperty("method", encrypt);

            String keyJsonString = new Gson().toJson(keyJson);

            return RSA.encryptWithPublicKey(keyJsonString, getKeyPair().getPublic());
        } catch (Exception e) {
            System.err.println("Error encrypting symmetric key: " + e.getMessage());
            return null;
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

    public KeyPair getKeyPair() {
        return keyPair;
    }

    private String decryptMessage(String message) {
        JsonObject messageAndKey = new Gson().fromJson(message, JsonObject.class);
        String encryptedMessage = messageAndKey.get("message").getAsString();
        String encryptedKey = messageAndKey.get("encryptedKey").getAsString();

        String decryptedKeyJson = null;
        try {
            decryptedKeyJson = RSA.decryptWithPrivateKey(encryptedKey, keyPair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JsonObject decryptedKeyJsonObject = new Gson().fromJson(decryptedKeyJson, JsonObject.class);
        String symmetricKey = decryptedKeyJsonObject.get("key").getAsString();
        key = symmetricKey;
        String encryptionMethod = decryptedKeyJsonObject.get("method").getAsString();
        encrypt = encryptionMethod;

        String decryptedMessage = "";
        if (encryptionMethod.contains("Caesar")) {
            CaesarCipher caesarCipher = new CaesarCipher();
            decryptedMessage = caesarCipher.decrypt(symmetricKey, encryptedMessage);
        } else if (encryptionMethod.contains("AES")) {
            AESCrypto aesCrypto = new AESCrypto();
            decryptedMessage = aesCrypto.decrypt(symmetricKey, encryptedMessage);
        }

        return decryptedMessage;
    }

    private void decryptSymmetricKey(String encryptedKey) {
        try {
            String decryptedKeyJson = RSA.decryptWithPrivateKey(encryptedKey, keyPair.getPrivate());
            JsonObject decryptedKeyJsonObject = new Gson().fromJson(decryptedKeyJson, JsonObject.class);
            key = decryptedKeyJsonObject.get("key").getAsString();
            encrypt = decryptedKeyJsonObject.get("method").getAsString();

            System.out.println("Decrypted symmetric key: " + key);
            System.out.println("Encryption method: " + encrypt);
        } catch (Exception e) {
            System.err.println("Error decrypting symmetric key: " + e.getMessage());
        }
    }
}
