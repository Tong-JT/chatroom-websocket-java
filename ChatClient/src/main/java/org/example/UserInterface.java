package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class UserInterface extends VBox {

    private VBox connectionSide;
    private HBox userBoxes;
    private VBox chatSide;
    private VBox encryptionSide;
    private Label serverStatus;

    public UserInterface() {
        connectionSide = new ConnectionSide(this);
        chatSide = new ChatSide();
        encryptionSide = new EncryptionSide();
        userBoxes = new HBox(chatSide, encryptionSide);

        userBoxes.setDisable(true);

        this.getChildren().addAll(connectionSide, userBoxes);
    }

    public void toggleUserBoxes() {
        if (userBoxes.isDisabled()) {
            userBoxes.setDisable(false);
        } else {
            userBoxes.setDisable(true);
        }
    }

    public void setServerStatus(String status) {
        serverStatus.setText(status);
    }
}
