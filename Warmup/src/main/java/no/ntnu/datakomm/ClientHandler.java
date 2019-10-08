package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class ClientHandler extends Thread {

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {

        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){

        try {
            InputStreamReader reader = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader buffReader = new BufferedReader(reader);

            String clientInput = buffReader.readLine();
            System.out.println("Client sent: " + clientInput);

            String response = "Hello Client";
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(response);
            System.out.println("Sent " + response + " to Client");

            // Close connection to this particular client
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
