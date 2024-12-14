package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Chatroom {
    private String name;
    private List<ClientThread> clients;

    public Chatroom(String name) {
        this.name = name;
        this.clients = new ArrayList<ClientThread>();
    }

    public String getName() {
        return name;
    }

    public int getUserNum() {
        return clients.size();
    }

    public void addClient(ClientThread client) {
        clients.add(client);
    }

    public void broadcastMessage(String message) {
        for (ClientThread client : clients) {
            client.sendChatToClient(message);
        }
    }

    public List<ClientThread> getClients() {
        return clients;
    }
}
