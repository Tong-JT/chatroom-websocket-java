package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client connected, sending hello world...");
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Hello, client! You've connected to the server.");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received message from client: " + clientMessage);

                out.println("Server received your message: " + clientMessage);

                if (clientMessage.equalsIgnoreCase("disconnect")) {
                    System.out.println("Client requested to disconnect.");
                    break;
                }
            }

            System.out.println("Client disconnected or closed connection.");
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Connection closed for " + clientSocket);
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
}
