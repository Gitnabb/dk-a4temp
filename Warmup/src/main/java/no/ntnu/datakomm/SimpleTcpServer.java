package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    private static final int PORT = 1301;
    
    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    public void run() {
        try {
            // TODO - implement the logic of the server, according to the protocol.
            // Take a look at the tutorial to understand the basic blocks: creating a listening socket,
            // accepting the next client connection, sending and receiving messages and closing the connection

            ServerSocket welcomeSocket = new ServerSocket(PORT);
            System.out.println("Connected to server on port " + PORT);
            
            Socket clientSocket = welcomeSocket.accept();
            
            InputStreamReader reader = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader bufReader = new BufferedReader(reader);
            
            String clientInput = bufReader.readLine();
            System.out.println("Client: " + clientInput);
            
            String response = "Hei!";
            
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(response);
            
            
            // close client socket
            clientSocket.close();

            
            // closing the welcome socket
            welcomeSocket.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message) {
        System.out.println(message);
    }
}
