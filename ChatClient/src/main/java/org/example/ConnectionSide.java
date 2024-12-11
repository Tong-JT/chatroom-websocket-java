package org.example;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ConnectionSide extends VBox {
    TextField ipField;
    TextField portField;
    Button searchServer;
    Label serverStatus;

    private ClientSocket clientSocket;

    public ConnectionSide() {
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

        searchServer = new Button("Find server");
        serverStatus = new Label();

        searchServer.setOnAction(e -> connectToServer());

        this.getChildren().addAll(ipLabel, ipField, portLabel, portField, searchServer, serverStatus);
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
            } else {
                serverStatus.setText("Failed to connect.");
            }
        } catch (NumberFormatException e) {
            serverStatus.setText("Invalid port number.");
        }
    }
}
