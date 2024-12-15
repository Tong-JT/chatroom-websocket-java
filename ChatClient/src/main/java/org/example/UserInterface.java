package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class UserInterface extends VBox {

    private ConnectionSide connectionSide;
    private HBox userBoxes;
    private ChatSide chatSide;
    private EncryptionSide encryptionSide;
    private VBox onScreen;
    private ChatroomSide chatroomSide;

    public UserInterface() {
        connectionSide = new ConnectionSide(this);
        chatSide = new ChatSide(connectionSide);
        encryptionSide = new EncryptionSide(connectionSide);

        userBoxes = new HBox(chatSide, encryptionSide);

        userBoxes = new HBox(chatSide, encryptionSide);

        HBox.setHgrow(chatSide, Priority.ALWAYS);
        HBox.setHgrow(encryptionSide, Priority.ALWAYS);

        userBoxes.setDisable(true);
        onScreen = new VBox();
        VBox.setVgrow(onScreen, Priority.ALWAYS);

        this.getChildren().add(onScreen);
        onScreen.getChildren().addAll(connectionSide, userBoxes);
    }

    public void toggleUserBoxes() {
        if (userBoxes.isDisabled()) {
            userBoxes.setDisable(false);
        } else {
            userBoxes.setDisable(true);
        }
    }

    public void enterChatroom(String name) throws IOException {
        onScreen.getChildren().clear();
        onScreen.getChildren().add(chatroomSide = new ChatroomSide(connectionSide, name));
        connectionSide.getClientSocket().setIsInChatroom(true);
        connectionSide.getClientSocket().startListeningForMessages();
    }

    public void leaveChatroom() {
        onScreen.getChildren().clear();
        onScreen.getChildren().addAll(connectionSide, userBoxes);
        connectionSide.getClientSocket().setIsInChatroom(false);
        connectionSide.getClientSocket().stopListeningForMessages();
        connectionSide.getClientSocket().close();
        connectionSide.resetUI();
        encryptionSide.resetUI();

        System.out.println("Left the chatroom, returned to homescreen.");
    }



    public ChatSide getChatSide() {
        return chatSide;
    }

    public ChatroomSide getChatroomSide() {
        return chatroomSide;
    }

    public EncryptionSide getEncryptionSide() {
        return encryptionSide;
    }
}

