package org.example;

import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.ClientSocket;
import org.example.ConnectionSide;

import java.util.HashMap;
import java.util.Map;

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
    private Button exchangeButton;
    private Label encryptionStatus;

    private ClientSocket clientSocket;
    private EncryptionMethod selectedEncryption;
    private String encryptedSymmetricKey;

    public EncryptionSide(ConnectionSide connectionSide) {
        this.clientSocket = connectionSide.getClientSocket();
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

        exchangeButton = new Button("Exchange Keys With Server");
        exchangeButton.setOnAction(e -> exchangeKeysWithServer());

        VBox caesarVBox = new VBox(caesarRadioButton, caesarKeyBox);
        VBox aesVBox = new VBox(aesRadioButton, aesKeyBox);

        encryptionStatus = new Label();

        HBox exportImportButtonsBox = new HBox(exportKeysButton, importKeysButton);
        this.getChildren().addAll(encryptionLabel, caesarVBox, aesVBox, exportImportButtonsBox, exchangeButton, encryptionStatus);
        disableAll();
        toggleEncryption(false);

        caesarGenerateKeyButton.setOnAction(e -> generateKey("Caesar"));
        aesGenerateKeyButton.setOnAction(e -> generateKey("AES"));
    }

    private void toggleEncryptionOptions() {
        if (caesarRadioButton.isSelected()) {
            selectedEncryption = new CaesarCipher();
            caesarKeyBox.setDisable(false);
            aesKeyBox.setDisable(true);
            aesKeyField.clear();
        } else if (aesRadioButton.isSelected()) {
            selectedEncryption = new AESCrypto();
            caesarKeyBox.setDisable(true);
            aesKeyBox.setDisable(false);
            caesarKeyField.clear();
        }
    }

    private void disableAll() {
        caesarKeyBox.setDisable(true);
        aesKeyBox.setDisable(true);
    }

    private String requestPublicKey() {
        clientSocket.sendMessage("RequestPublicKey");
        return clientSocket.receivePublicKey();
    }

    private void exchangeKeysWithServer() {
        String publicKey = requestPublicKey();

        if (publicKey != null) {
            String selectedEncryptionMethod = selectedEncryption.getClass().getSimpleName();
            String key = "";

            if (selectedEncryptionMethod.equals("CaesarCipher")) {
                key = caesarKeyField.getText();
            } else if (selectedEncryptionMethod.equals("AESCrypto")) {
                key = aesKeyField.getText();
            }

            try {
                JsonObject methodAndKey = new JsonObject();
                methodAndKey.addProperty("method", selectedEncryptionMethod);
                methodAndKey.addProperty("key", key);
                String JSONtoEncrypt = methodAndKey.toString();
                String encryptedKey = RSA.encryptWithPublicKey(JSONtoEncrypt, RSA.stringToPublicKey(publicKey));

                encryptedSymmetricKey = encryptedKey;

                System.out.println("Encrypted symmetric key: " + encryptedSymmetricKey);

            } catch (Exception e) {
                System.err.println("Error during encryption: " + e.getMessage());
            }
        } else {
            System.err.println("Public key request failed.");
        }
    }

    private void generateKey(String encryptionType) {
        String randomKey = "";
        randomKey = selectedEncryption.generateKey();

        if (encryptionType.equals("Caesar")) {
            caesarKeyField.setText(randomKey);
        } else if (encryptionType.equals("AES")) {
            aesKeyField.setText(randomKey);
        }
    }

    public void toggleEncryption(boolean encrypted) {
        if (encrypted) {
            encryptionStatus.setText("Chat messages encrypted");
            encryptionStatus.setStyle("-fx-text-fill: green;");
        }
        else {
            encryptionStatus.setText("Chat messages not encrypted");
            encryptionStatus.setStyle("-fx-text-fill: red;");
        }
    }

    public EncryptionMethod getSelectedEncryption() {
        return selectedEncryption;
    }

    public String getSymmetricKey() {
        if (selectedEncryption instanceof CaesarCipher) {
            return caesarKeyField.getText();
        } else if (selectedEncryption instanceof AESCrypto) {
            return aesKeyField.getText();
        }
        return "";
    }

    public String getEncryptedSymmetricKey() {
        return encryptedSymmetricKey;
    }

}
