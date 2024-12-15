package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ChatroomSide extends VBox {

    private ConnectionSide connectionSide;
    private String name;
    private HBox topBar;
    private Label title;
    private Button leaveChat;
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;

    public ChatroomSide(ConnectionSide connectionSide, String name) {
        this.connectionSide = connectionSide;
        this.name = name;

        title = new Label(name);
        title.getStyleClass().add("title");
        Label loggedIn = new Label(" (Logged in as " + connectionSide.getUsername() + ")");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        leaveChat = new Button("Exit");
        topBar = new HBox(title, loggedIn, spacer, leaveChat);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        ScrollPane chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        inputField = new TextField();
        inputField.getStyleClass().add("midtext");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendButton = new Button("Send message");
        sendButton.setOnAction(event -> sendMessage());

        HBox inputBox = new HBox(inputField, sendButton);
        inputBox.setSpacing(10);
        inputBox.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(inputBox, Priority.NEVER);

        this.getChildren().addAll(topBar, chatArea, inputBox);
        this.setSpacing(10);
        this.getStyleClass().add("section");
        this.setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        leaveChat.setOnAction(event -> {
            connectionSide.getUserInterface().leaveChatroom();
        });
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            inputField.clear();
            String encryptedMessage = encryptMessage(message);
            String encryptedKey = connectionSide.getUserInterface().getEncryptionSide().getEncryptedSymmetricKey();

            JsonObject messageAndKey = new JsonObject();
            messageAndKey.addProperty("message", encryptedMessage);
            messageAndKey.addProperty("encryptedKey", encryptedKey);

            String fullMessage = "ChatMessage" + messageAndKey.toString();

            connectionSide.getClientSocket().sendMessage(fullMessage);
        }
    }

    private String encryptMessage(String message) {
        String symmetricKey = connectionSide.getUserInterface().getEncryptionSide().getSymmetricKey();
        EncryptionMethod selectedEncryption = connectionSide.getUserInterface().getEncryptionSide().getSelectedEncryption();
        return selectedEncryption.encrypt(symmetricKey, message);
    }

    public void printMessage(String text) {
        JsonObject messageAndKey = new Gson().fromJson(text, JsonObject.class);

        String encryptedMessage = messageAndKey.get("message").getAsString();
        String symmetricKey = connectionSide.getUserInterface().getEncryptionSide().getSymmetricKey();
        EncryptionMethod selectedEncryption = connectionSide.getUserInterface().getEncryptionSide().getSelectedEncryption();

        String decryptedMessage = selectedEncryption.decrypt(symmetricKey, encryptedMessage);

        chatArea.appendText(decryptedMessage + "\n");
    }
}
