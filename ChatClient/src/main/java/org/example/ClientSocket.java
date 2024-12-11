package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    private Socket clientSocket;

    public boolean connect(String serverAddress, int port) {
        try {
            clientSocket = new Socket(serverAddress, port);
            System.out.println("Connected to the server!");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }

    public void receiveMessage() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = in.readLine();
                System.out.println("Received from server: " + message);
            } catch (IOException e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
    }

    public void close() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
