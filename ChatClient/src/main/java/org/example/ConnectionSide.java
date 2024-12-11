package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ConnectionSide extends VBox {
    VBox IPInputs;
    TextField ipField;
    TextField portField;
    Button searchServer;
    Label serverStatus;

    private ClientSocket clientSocket;
    private UserInterface userInterface;

    public ConnectionSide(UserInterface userInterface) {
        this.userInterface = userInterface;
        initialiseUI();
        clientSocket = new ClientSocket();
    }

    public void initialiseUI() {
        Label ipLabel = new Label("IP Address:");
        ipField = new TextField();
        ipField.setPromptText("Enter IP (eg. localhost)");

        Label portLabel = new Label("Port:");
        portField = new TextField();
        portField.setPromptText("Enter port (eg. 8080)");

        IPInputs = new VBox();
        IPInputs.getChildren().addAll(ipLabel, ipField, portLabel, portField);
        searchServer = new Button("Find server");
        serverStatus = new Label();

        buttonEventListener();

        this.getChildren().addAll(IPInputs, searchServer, serverStatus);
    }

    private void connectToServer() {
        String serverAddress = ipField.getText();
        String portText = portField.getText();

        if (serverAddress.isEmpty() || portText.isEmpty()) {
            serverStatus.setText("Please provide both IP and Port.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            boolean isConnected = clientSocket.connect(serverAddress, port);
            if (isConnected) {
                serverStatus.setText("Connected to the server!");
                clientSocket.sendMessage("Hello from client!");
                searchServer.setText("Disconnect");
                userInterface.toggleUserBoxes();
                toggleIPInputs();

            } else {
                serverStatus.setText("Failed to connect.");
            }
        } catch (NumberFormatException e) {
            serverStatus.setText("Invalid port number.");
        }
    }

    private void toggleIPInputs() {
        if (IPInputs.isDisabled()) {
            IPInputs.setDisable(false);
        } else {
            IPInputs.setDisable(true);
        }
    }

    private void disconnectFromServer() {
        clientSocket.sendMessage("Disconnect");
        searchServer.setText("Find server");
        userInterface.toggleUserBoxes();
        toggleIPInputs();
    }

    public void buttonEventListener() {
        searchServer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchServer.getText().equals("Find server")) {
                    connectToServer();
                } else if (searchServer.getText().equals("Disconnect")) {
                    disconnectFromServer();
                }
            }
        });
    }
}
