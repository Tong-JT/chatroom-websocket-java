package org.example;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreService {

    private GoogleCredentials authExplicit(String jsonPath) throws IOException {
        GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        return creds;
    }

    private Firestore getInstance(GoogleCredentials credentials, String projectId) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        return FirestoreClient.getFirestore();
    }

    public String addUser(String jsonPath, String projectId, String username, KeyPair keyPair) throws IOException, ExecutionException, InterruptedException {
        GoogleCredentials credentials = authExplicit(jsonPath);
        Firestore db = getInstance(credentials, projectId);

        String publicKeyString = RSA.publicKeyToString(keyPair.getPublic());
        String privateKeyString = RSA.privateKeyToString(keyPair.getPrivate());

        DocumentReference docRef = db.collection("users").document(username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("publicKey", publicKeyString);
        data.put("privateKey", privateKeyString);

        ApiFuture<WriteResult> result = docRef.set(data);
        return "User added, update time: " + result.get().getUpdateTime();
    }

    public String addChatroom(String jsonPath, String projectId, String name) throws IOException, ExecutionException, InterruptedException {
        GoogleCredentials credentials = authExplicit(jsonPath);
        Firestore db = getInstance(credentials, projectId);

        DocumentReference docRef = db.collection("chatrooms").document(name);  // Collection is now "chatrooms"
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        ApiFuture<WriteResult> result = docRef.set(data);
        return "Chatroom added, update time: " + result.get().getUpdateTime();
    }

    public String addChatLog(String jsonPath, String projectId, String chatroomName, String username, String message) throws IOException, ExecutionException, InterruptedException {
        GoogleCredentials credentials = authExplicit(jsonPath);
        Firestore db = getInstance(credentials, projectId);

        DocumentReference docRef = db.collection("chatrooms").document(chatroomName).collection("messages").document();  // Changed from "groupChats" to "chatrooms"
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("timestamp", System.currentTimeMillis());
        data.put("message", message);

        ApiFuture<WriteResult> result = docRef.set(data);
        return "Chat log added, update time: " + result.get().getUpdateTime();
    }
}
