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

public class ChatSide extends VBox {
    HBox nameBox;
    TextField usernameField;
    VBox newChatroomBox;
    TextField chatField;
    Button confirmNewChatroom;
    HBox buttonSwitches;
    Button newChatroom;
    Button viewChatrooms;
    VBox optionsHolder;

    public ChatSide() {
        initialiseGUI();
    }

    public void initialiseGUI() {
        nameBox = new HBox();
        nameBox.getChildren().addAll(
                new Label("Screen Name:"),
                usernameField = new TextField());

        buttonSwitches = new HBox();
        buttonSwitches.getChildren().addAll(
                newChatroom = new Button("Create Chatroom"),
                viewChatrooms = new Button("View Chatrooms"));

        VBox optionsHolderBox = new VBox();
        optionsHolder = new VBox();
        optionsHolderBox.getChildren().add(optionsHolder);
        optionsHolderBox.setPrefWidth(200);
        optionsHolderBox.setPrefHeight(200);

        this.getChildren().addAll(nameBox, buttonSwitches, optionsHolderBox);

        newChatroom();
        addEventListeners();
    }

    public void newChatroom() {
        newChatroomBox = new VBox();
        Label chatLabel = new Label("Chatroom Name:");
        chatField = new TextField();
        confirmNewChatroom = new Button("Confirm");

        newChatroomBox.getChildren().addAll(chatLabel, chatField, confirmNewChatroom);
    }

    public void addEventListeners() {
        newChatroom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                optionsHolder.getChildren().add(newChatroomBox);
            }
        });
        viewChatrooms.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                optionsHolder.getChildren().removeAll();
            }
        });
    }

}
