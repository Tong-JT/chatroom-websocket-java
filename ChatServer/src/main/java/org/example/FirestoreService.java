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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreService {

    // Method to authenticate and return Google credentials
    private GoogleCredentials authExplicit(String jsonPath) throws IOException {
        GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        return creds;
    }

    // Method to initialize the Firestore instance
    private Firestore getInstance(GoogleCredentials credentials, String projectId) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
        FirebaseApp.initializeApp(options);
        return FirestoreClient.getFirestore();
    }

    // Method to add user data to Firestore
    public String addUser(String jsonPath, String projectId, String firstName, String lastName, int birthYear) throws IOException, ExecutionException, InterruptedException {
        GoogleCredentials credentials = authExplicit(jsonPath);
        Firestore db = getInstance(credentials, projectId);

        DocumentReference docRef = db.collection("users").document("alovelace");
        Map<String, Object> data = new HashMap<>();
        data.put("first", firstName);
        data.put("last", lastName);
        data.put("born", birthYear);

        ApiFuture<WriteResult> result = docRef.set(data);
        return "Update time: " + result.get().getUpdateTime();
    }
}
