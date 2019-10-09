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
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);


            // Prints server version to Client as first write in connection.
            writer.println(SimpleTcpServer.VERSION);

            String clientInput = buffReader.readLine();
            System.out.println("Client sent: " + clientInput);

            String response = "Hello Client";
            writer.println(response);

            // Close connection to this particular client
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
