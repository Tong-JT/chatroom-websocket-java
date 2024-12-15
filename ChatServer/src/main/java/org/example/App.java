package org.example;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class App {


    public App() throws IOException, ExecutionException, InterruptedException {
        Server server = new Server();
        server.startServer();
    }

    public static void main(String[] args) {
        System.out.println("Program begin...");
        try {
            new App();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Program end.");
    }
}
