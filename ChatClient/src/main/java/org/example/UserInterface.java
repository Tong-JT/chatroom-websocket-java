package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class UserInterface extends VBox {

    private VBox connectionSide;
    private HBox userBoxes;
    private VBox chatSide;
    private VBox encryptionSide;
    private Label serverStatus;

    public UserInterface() {
        connectionSide = new ConnectionSide();
        chatSide = new ChatSide();
        encryptionSide = new EncryptionSide();
        userBoxes = new HBox(chatSide, encryptionSide);
        this.getChildren().addAll(connectionSide, userBoxes);
    }

    public void setServerStatus(String status) {
        serverStatus.setText(status);
    }
}
