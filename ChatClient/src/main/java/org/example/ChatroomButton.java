package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

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
        holder = new HBox(nameLabel, num);

        this.getChildren().add(holder);
    }
}
