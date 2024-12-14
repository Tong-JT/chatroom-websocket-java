package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.event.HierarchyBoundsListener;
import java.util.List;
import java.util.Map;

public class ChatSide extends VBox {
    VBox newChatroomBox;
    TextField chatField;
    Button confirmNewChatroom;
    HBox buttonSwitches;
    Button newChatroom;
    Button viewChatrooms;
    VBox optionsHolder;
    VBox chatroomListBox;
    ConnectionSide connectionSide;

    public ChatSide(ConnectionSide connectionSide) {
        initialiseGUI();
        this.connectionSide = connectionSide;
    }

    public void initialiseGUI() {
        buttonSwitches = new HBox();
        buttonSwitches.getChildren().addAll(
                newChatroom = new Button("Create Chatroom"),
                viewChatrooms = new Button("View Chatrooms"));

        VBox optionsHolderBox = new VBox();
        optionsHolder = new VBox();
        optionsHolderBox.getChildren().add(optionsHolder);
        optionsHolderBox.setPrefWidth(200);
        optionsHolderBox.setPrefHeight(200);

        this.getChildren().addAll(buttonSwitches, optionsHolderBox);

        newChatroom();
        chatroomListBox = new VBox();
        addEventListeners();
    }

    public void newChatroom() {
        newChatroomBox = new VBox();
        Label chatLabel = new Label("Chatroom Name:");
        chatField = new TextField();
        confirmNewChatroom = new Button("Confirm");

        newChatroomBox.getChildren().addAll(chatLabel, chatField, confirmNewChatroom);

        confirmNewChatroom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String chatroomName = chatField.getText().trim();
                if (!chatroomName.isEmpty()) {
                    connectionSide.getClientSocket().sendMessage("CreateChatroom" + chatroomName);
                    System.out.println("Requesting server to create chatroom: " + chatroomName);
                    connectionSide.getClientSocket().receiveMessage();
                    chatField.clear();
                }
            }
        });
    }

    public void addEventListeners() {
        newChatroom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                optionsHolder.getChildren().clear();
                optionsHolder.getChildren().add(newChatroomBox);
            }
        });
        viewChatrooms.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                connectionSide.getClientSocket().sendMessage("ListChatrooms");
                connectionSide.getClientSocket().receiveMessage();
            }
        });
    }

    public void updateChatroomList(List<Map<String, Object>> chatrooms) {
        chatroomListBox.getChildren().clear();
        if (chatrooms.isEmpty()) {
            chatroomListBox.getChildren().add(new Label("No chatrooms available"));
        } else {
            for (Map<String, Object> chatroom : chatrooms) {
                String chatroomName = (String) chatroom.get("name");
                Double clientCountDouble = (Double) chatroom.get("clientCount");
                int clientCount = clientCountDouble.intValue();

                ChatroomButton chatroomButton = new ChatroomButton(chatroomName, clientCount);
                chatroomButton.setOnMouseClicked(event -> {
                    if (connectionSide.getUserInterface().getEncryptionSide().getEncryptionStatus()) {
                        connectionSide.getClientSocket().sendMessage("JoinChatroom" + chatroomName);
                        System.out.println("Attempting to join chatroom: " + chatroomName);
                        connectionSide.getClientSocket().receiveMessage();
                    }
                });
                chatroomListBox.getChildren().add(chatroomButton);
            }
        }
        optionsHolder.getChildren().clear();
        optionsHolder.getChildren().add(chatroomListBox);
    }

}
