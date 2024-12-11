package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public void startServer() throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        try {
            while (true) {
                System.out.println("Waiting on client to connect.");
                Socket clientSocket = serverSocket.accept();
                new ClientThread(clientSocket).start();
                System.out.println("Opened new thread");
            }
        } finally {

        }
    }
}
