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
import javafx.stage.FileChooser;
import org.example.ClientSocket;
import org.example.ConnectionSide;

import java.io.*;
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

    private VBox publicKeyBox;
    private Button publicKeyButton;

    private ClientSocket clientSocket;
    private EncryptionMethod selectedEncryption;
    private String encryptedSymmetricKey;
    private String publicKey;

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

        exportKeysButton.setOnAction(e -> exportKeys());
        importKeysButton.setOnAction(e -> importKeys());

        caesarKeyBox = new HBox(caesarKeyField, caesarGenerateKeyButton);
        aesKeyBox = new HBox(aesKeyField, aesGenerateKeyButton);

        exchangeButton = new Button("Exchange Keys With Server");
        exchangeButton.setOnAction(e -> exchangeKeysWithServer());

        VBox caesarVBox = new VBox(caesarRadioButton, caesarKeyBox);
        VBox aesVBox = new VBox(aesRadioButton, aesKeyBox);

        encryptionStatus = new Label();

        HBox exportImportButtonsBox = new HBox(exportKeysButton, importKeysButton);

        publicKeyBox = new VBox(new Label("Warning: This public key will only be generated on this screen. Be sure to store it somewhere you can find it again."), publicKeyButton = new Button("Export Public Key"));
        publicKeyButton.setOnAction(e -> exportPublicKey());
        this.getChildren().addAll(encryptionLabel, caesarVBox, aesVBox, exportImportButtonsBox, exchangeButton, encryptionStatus, publicKeyBox);
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
        publicKey = requestPublicKey();

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

                encryptedSymmetricKey = RSA.encryptWithPublicKey(JSONtoEncrypt, RSA.stringToPublicKey(publicKey));

                System.out.println("Encrypted symmetric key: " + encryptedSymmetricKey);

                clientSocket.sendMessage("NewSymmetricKey" + encryptedSymmetricKey);
                clientSocket.receiveMessage();

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

    public Boolean getEncryptionStatus() {
        if (encryptionStatus.getText().equals("Chat messages encrypted")) {
            return true;
        }
        return false;
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

    private void exportKeys() {
        EncryptionMethod encryptionMethod = getSelectedEncryption();
        String key = getSymmetricKey();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object Files", "*.obj"));
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                objectOutputStream.writeObject(encryptionMethod.getClass().getSimpleName());
                objectOutputStream.writeObject(key);
                System.out.println("Key exported successfully.");
            } catch (IOException e) {
                System.err.println("Error exporting key: " + e.getMessage());
            }
        }
    }

    private void importKeys() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object Files", "*.obj"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(selectedFile))) {
                String methodName = (String) objectInputStream.readObject();
                String key = (String) objectInputStream.readObject();

                if (methodName.equals("CaesarCipher")) {
                    selectedEncryption = new CaesarCipher();
                    caesarKeyField.setText(key);
                    caesarKeyBox.setDisable(false);
                    aesKeyBox.setDisable(true);
                    caesarRadioButton.setSelected(true);
                } else if (methodName.equals("AESCrypto")) {
                    selectedEncryption = new AESCrypto();
                    aesKeyField.setText(key);
                    aesKeyBox.setDisable(false);
                    caesarKeyBox.setDisable(true);
                    aesRadioButton.setSelected(true);
                }
                System.out.println("Key imported successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error importing key: " + e.getMessage());
            }
        }
    }

    private void exportPublicKey() {
        if (publicKey != null && !publicKey.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object Files", "*.obj"));
            File selectedFile = fileChooser.showSaveDialog(null);

            if (selectedFile != null) {
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                    objectOutputStream.writeObject(publicKey);
                    System.out.println("Public key exported successfully.");
                } catch (IOException e) {
                    System.err.println("Error exporting public key: " + e.getMessage());
                }
            }
        } else {
            System.err.println("Public key is not available.");
        }
    }

}
