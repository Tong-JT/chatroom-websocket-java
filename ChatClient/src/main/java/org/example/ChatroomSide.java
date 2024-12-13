package org.example;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
        leaveChat = new Button("Exit");
        topBar = new HBox(title, leaveChat);
        chatArea = new TextArea();
        chatArea.setEditable(false);

        inputField = new TextField();

        sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage());

        HBox inputBox = new HBox(inputField, sendButton);
        inputBox.setSpacing(5);

        this.getChildren().addAll(topBar, chatArea, inputBox);

        leaveChat.setOnAction(event -> {
            connectionSide.getUserInterface().leaveChatroom();
        });
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            inputField.clear();
            String fullMessage = "ChatMessage" + message;
            connectionSide.getClientSocket().sendMessage(fullMessage);
        }
    }

    public void printMessage(String text) {
        chatArea.appendText(text + "\n");
    }
}

