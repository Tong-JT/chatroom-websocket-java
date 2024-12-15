package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConnectionSide extends VBox {
    VBox IPInputs;
    TextField ipField;
    TextField portField;
    Button searchServer;
    Label serverStatus;
    HBox nameBox;
    TextField usernameField;
    Label usernameTitle;
    String username;

    private ClientSocket clientSocket;
    private UserInterface userInterface;

    public ConnectionSide(UserInterface userInterface) {
        this.userInterface = userInterface;
        initialiseUI();
        clientSocket = new ClientSocket(userInterface);
    }

    public void initialiseUI() {
        nameBox = new HBox();
        nameBox.setSpacing(5);
        nameBox.getChildren().addAll(
                usernameTitle = new Label("Welcome! Please enter your username: "),
                usernameField = new TextField());
        nameBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        usernameTitle.getStyleClass().add("title");
        Label ipLabel = new Label("IP Address:");
        ipField = new TextField();
        ipField.setPromptText("Enter IP (use localhost)");

        Label portLabel = new Label("Port:");
        portField = new TextField();
        portField.setPromptText("Enter port (use 8080)");

        IPInputs = new VBox();
        IPInputs.setSpacing(5);
        IPInputs.getChildren().addAll(ipLabel, ipField, portLabel, portField);
        searchServer = new Button("Find server");
        VBox.setMargin(searchServer, new Insets(8, 0, 0, 0));
        serverStatus = new Label();
        serverStatus.setText("No connection.");
        serverStatus.setStyle("-fx-text-fill: red;");

        buttonEventListener();

        this.setSpacing(10);
        this.getChildren().addAll(nameBox, IPInputs, searchServer, serverStatus);
        this.getStyleClass().add("section");
        this.getStyleClass().add("connectionside");
    }

    public void connectToServer() {
        String serverAddress = ipField.getText();
        String portText = portField.getText();
        username = usernameField.getText();

        if (serverAddress.isEmpty() || portText.isEmpty() || username.isEmpty()) {
            serverStatus.setText("Please provide IP, Port, and Username.");
            serverStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int port = Integer.parseInt(portText);

            if (clientSocket != null) {
                clientSocket.close();
            }

            boolean isConnected = clientSocket.connect(serverAddress, port);
            if (isConnected) {
                serverStatus.setText("Connected to the server!");
                serverStatus.setStyle("-fx-text-fill: #37ed3d;");
                clientSocket.sendMessage("Username" + username);
                searchServer.setText("Disconnect");
                userInterface.toggleUserBoxes();
                toggleIPInputs();
            } else {
                serverStatus.setText("Failed to connect.");
                serverStatus.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            serverStatus.setText("Invalid port number.");
            serverStatus.setStyle("-fx-text-fill: red;");
        }
    }


    private void toggleIPInputs() {
        if (IPInputs.isDisabled()) {
            IPInputs.setDisable(false);
            IPInputs.setVisible(true);
            ipField.setVisible(true);
            portField.setVisible(true);
            usernameField.setDisable(false);
            IPInputs.setManaged(true);
            ipField.setManaged(true);
            portField.setManaged(true);
        } else {
            IPInputs.setDisable(true);
            IPInputs.setVisible(false);
            ipField.setVisible(false);
            portField.setVisible(false);
            usernameField.setDisable(true);
            IPInputs.setManaged(false);
            ipField.setManaged(false);
            portField.setManaged(false);
        }
    }

    private void disconnectFromServer() {
        clientSocket.sendMessage("Disconnect");
        searchServer.setText("Find server");
        userInterface.toggleUserBoxes();
        toggleIPInputs();
        serverStatus.setText("No connection.");
        serverStatus.setStyle("-fx-text-fill: red;");
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

    public ClientSocket getClientSocket() {
        return clientSocket;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public void resetUI() {
        serverStatus.setText("No connection.");
        serverStatus.setStyle("-fx-text-fill: red;");
        searchServer.setText("Find server");

        ipField.clear();
        portField.clear();
        usernameField.clear();

        toggleIPInputs();
        userInterface.toggleUserBoxes();
    }

    public String getUsername() {
        return username;
    }
}
