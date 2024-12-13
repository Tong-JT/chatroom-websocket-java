package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

public class EncryptionSide extends VBox {
    private RadioButton caesarRadioButton;
    private RadioButton aesRadioButton;
    private TextField caesarKeyField;
    private TextField aesKeyField;
    private Button caesarGenerateKeyButton;
    private Button aesGenerateKeyButton;
    private HBox caesarKeyBox;
    private HBox aesKeyBox;
    private Button exportKeysButton;
    private Button importKeysButton;

    public EncryptionSide(ConnectionSide connectionSide) {
        initialiseGUI();
    }

    public void initialiseGUI() {
        Label encryptionLabel = new Label("Encryption Settings:");
        ToggleGroup encryptionGroup = new ToggleGroup();

        caesarRadioButton = new RadioButton("Caesar Cipher");
        caesarRadioButton.setOnAction(e -> toggleEncryptionOptions());
        caesarRadioButton.setToggleGroup(encryptionGroup);
        caesarKeyField = new TextField();
        caesarKeyField.setPromptText("Enter Caesar Key");
        caesarGenerateKeyButton = new Button("Generate Random");

        aesRadioButton = new RadioButton("AES Encryption");
        aesRadioButton.setOnAction(e -> toggleEncryptionOptions());
        aesRadioButton.setToggleGroup(encryptionGroup);
        aesKeyField = new TextField();
        aesKeyField.setPromptText("Enter AES Key");
        aesGenerateKeyButton = new Button("Generate Random");

        exportKeysButton = new Button("Export Keys");
        importKeysButton = new Button("Import Keys");

        caesarKeyBox = new HBox(caesarKeyField, caesarGenerateKeyButton);
        aesKeyBox = new HBox(aesKeyField, aesGenerateKeyButton);

        VBox caesarVBox = new VBox(caesarRadioButton, caesarKeyBox);
        VBox aesVBox = new VBox(aesRadioButton, aesKeyBox);

        HBox exportImportButtonsBox = new HBox(exportKeysButton, importKeysButton);
        this.getChildren().addAll(encryptionLabel, caesarVBox, aesVBox, exportImportButtonsBox);
        disableAll();
    }

    private void toggleEncryptionOptions() {
        boolean isCaesarSelected = caesarRadioButton.isSelected();

        caesarKeyBox.setDisable(!isCaesarSelected);
        aesKeyBox.setDisable(isCaesarSelected);
    }

    private void disableAll() {
        caesarKeyBox.setDisable(true);
        aesKeyBox.setDisable(true);
    }
}
