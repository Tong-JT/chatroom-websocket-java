package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EncryptionSide extends VBox {
    public EncryptionSide(ConnectionSide connectionSide) {
        initialiseGUI();
    }

    public void initialiseGUI() {
        Label encryptionLabel = new Label("Encryption Settings:");
        Button adjustEncryptionButton = new Button("Adjust Encryption");
        Button exportKeysButton = new Button("Export Keys");

        this.getChildren().addAll(encryptionLabel, adjustEncryptionButton, exportKeysButton);

    }
}
