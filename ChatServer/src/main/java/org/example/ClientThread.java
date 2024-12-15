package org.example;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.collect.Lists;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Map;
import java.util.Properties;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String encrypt;
    private String key;
    private KeyPair keyPair;
    private FirestoreService firestore;
    private String jsonPath;
    private String projectID;

    public ClientThread(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        if (keyPair == null) {
            generateKeyPair();
        }

        try {
            initializeStreams();

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received message from client: " + clientMessage);
                handleClientMessage(clientMessage);
            }

            handleClientDisconnection();

            System.out.println("Client disconnected or closed connection.");
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
            handleClientDisconnection();
        } finally {
            closeConnection();
        }
    }

    private void generateKeyPair() {
        try {
            keyPair = RSA.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Error generating key pair at server startup: " + e.getMessage());
        }
    }

    private void initializeStreams() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private void handleClientMessage(String clientMessage) {
        if (clientMessage.contains("Username")) {
            handleUsernameMessage(clientMessage);
        } else if (clientMessage.equalsIgnoreCase("ListChatrooms")) {
            listChatrooms();
        } else if (clientMessage.contains("JoinChatroom")) {
            joinChatroom(clientMessage);
        } else if (clientMessage.equalsIgnoreCase("Disconnect")) {
            handleDisconnectRequest();
        } else if (clientMessage.contains("ChatMessage")) {
            handleChatMessage(clientMessage);
        } else if (clientMessage.contains("CreateChatroom")) {
            createChatroom(clientMessage);
        } else if (clientMessage.equalsIgnoreCase("RequestPublicKey")) {
            handlePublicKeyRequest();
        } else if (clientMessage.contains("NewSymmetricKey")) {
            handleNewSymmetricKey(clientMessage);
        }
    }

    private void handleUsernameMessage(String clientMessage) {
        firestore = new FirestoreService();
        loadProperties();
        username = clientMessage.substring("Username".length()).trim();
        server.getClientsOnline().put(username, this);
        System.out.println("Client connected with username: " + username);
        try {
            firestore.addUser(jsonPath, projectID, username, keyPair);
        } catch (Exception e) {
            System.err.println("Error adding user to Firestore: " + e.getMessage());
        }
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("firebase.properties")) {
            properties.load(input);
            jsonPath = properties.getProperty("jsonpath");
            projectID = properties.getProperty("projectid");
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties", e);
        }
    }

    private void listChatrooms() {
        String chatroomsList = server.listChatrooms();
        StringBuilder finalString = new StringBuilder("ListChatrooms" + chatroomsList);
        out.println(finalString);
    }

    private void joinChatroom(String clientMessage) {
        String chatroomName = clientMessage.substring("JoinChatroom".length()).trim();
        server.addClientToChatroom(this, chatroomName);
    }

    private void handleDisconnectRequest() {
        System.out.println("Client requested to disconnect.");
    }

    private void handleChatMessage(String clientMessage) {
        String message = clientMessage.substring("ChatMessage".length()).trim();
        String decryptedMessage = decryptMessage(message);
        String newMessage = "[" + username + "] " + decryptedMessage;
        server.broadcastMessage(newMessage, this);

        try {
            String groupId = server.findChatroomFromClient(this).getName();
            firestore.addChatLog(jsonPath, projectID, groupId, username, message);
        } catch (Exception e) {
            System.err.println("Error saving chat log to Firestore: " + e.getMessage());
        }
    }

    private void createChatroom(String clientMessage) {
        String chatroomName = clientMessage.substring("CreateChatroom".length()).trim();
        server.createChatroom(chatroomName);
        sendMessageToClient("ChatroomCreated" + chatroomName);
    }

    private void handlePublicKeyRequest() {
        try {
            String publicKeyString = RSA.publicKeyToString(getKeyPair().getPublic());
            sendMessageToClient("PublicKey" + publicKeyString);
        } catch (Exception e) {
            System.err.println("Error sending public key: " + e.getMessage());
        }
    }

    private void handleNewSymmetricKey(String clientMessage) {
        String encryptedKey = clientMessage.substring("NewSymmetricKey".length()).trim();
        decryptSymmetricKey(encryptedKey);
        sendMessageToClient("New key processed");
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

    private void closeConnection() {
        try {
            clientSocket.close();
            System.out.println("Connection closed for " + clientSocket);
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
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
            chatroom.broadcastMessage(username + " has disconnected from " + chatroom.getName());
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
