package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ChatroomButton extends HBox {

    String name;
    int numClients;
    HBox holder;
    Label nameLabel;
    Label num;

    public ChatroomButton(String name, int numClients) {
        this.name = name;
        this.numClients = numClients;
        initialiseUI();
    }

    public void initialiseUI() {
        nameLabel = new Label(name);
        num = new Label(String.valueOf(numClients));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        this.getStyleClass().add("chatroombutton");
        this.getChildren().addAll(nameLabel, spacer, num);
    }
}
