package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;



/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpClient {
    // Remote host where the server will be running
    private static final String HOST = "localhost";
    // TCP port
    private static final int PORT = 1301;

    private Socket socket;
    /**
     * Run the TCP Client.
     *
     * @param args Command line arguments. Not used.
     */
    public static void main(String[] args) {
        SimpleTcpClient client = new SimpleTcpClient();
        try {
            client.run();
        } catch (InterruptedException e) {
            log("Client interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Run the TCP Client application. The logic is already implemented, no need to change anything in this method.
     * You can experiment, of course.
     *
     * @throws InterruptedException The method sleeps to simulate long client-server conversation.
     *                              This exception is thrown if the execution is interrupted halfway.
     */
    public void run() throws InterruptedException {
        log("Simple TCP client started");

        if (connectToServer(HOST, PORT)) {
            log("Connection to the server established");
            int a = (int) (1 + Math.random() * 10);
            int b = (int) (1 + Math.random() * 10);
            String request = a + "+" + b;
            if (sendRequestToServer(request)) {
                log("Sent " + request + " to server");
                String response = readResponseFromServer();
                if (response != null) {
                    log("Server responded with: " + response);
                    int secondsToSleep = 2 + (int)(Math.random() * 5);
                    log("Sleeping " + secondsToSleep + " seconds to allow simulate long client-server connection...");
                    Thread.sleep(secondsToSleep * 1000);
                    request = "bla+bla";
                    if (sendRequestToServer(request)) {
                        log("Sent " + request + " to server");
                        response = readResponseFromServer();
                        if (response != null) {
                            log("Server responded with: " + response);
                            if (sendRequestToServer("game over") && closeConnection()) {
                                log("Game over, connection closed");
                                // When the connection is closed, try to send one more message. It should fail.
                                if (!sendRequestToServer("2+2")) {
                                    log("Sending another message after closing the connection failed as expected");
                                } else {
                                    log("ERROR 7: sending a message after closing the connection did not fail!");
                                }
                            } else {
                                log("ERROR 6: Failed to stop conversation");
                            }
                        } else {
                            log("ERROR 5: Failed to receive server's response!");
                        }
                    } else {
                        log("ERROR 4: Failed to send invalid message to server!");
                    }
                } else {
                    log("ERROR 3: Failed to receive server's response!");
                }
            } else {
                log("ERROR 2: Failed to send valid message to server!");
            }
        } else {
            log("ERROR 1: Failed to connect to the server");
        }

        log("Simple TCP client finished");
    }

    /**
     * Close the TCP connection to the remote server.
     *
     * @return True on success, false otherwise
     */
    private boolean closeConnection() {
        try {
            this.socket.close();
            return true;
        } catch (Exception ex) {
            System.out.println("SOME ERROR in closing socket: " + ex);
        }
        
        return false;
    }

    /**
     * Try to establish TCP connection to the server (the three-way handshake).
     *
     * @param host The remote host to connect to. Can be domain (localhost, ntnu.no, etc), or IP address
     * @param port TCP port to use
     * @return True when connection established, false otherwise
     */
    private boolean connectToServer(String host, int port) {
        try {
            // TODO - implement this method
            // Remember to catch all possible exceptions that the Socket class can throw.
            this.socket = new Socket(host, port);
            return true;
        } 
        catch (UnknownHostException uhe){
            System.out.println(uhe);
            return false;
        }
        catch (IOException ioex) {
            System.out.println("SOME ERROR in connectToServer: " + ioex);
            return false;
        }
    }

    /**
     * Send a request message to the server (newline will be added automatically)
     *
     * @param request The request message to send. Do NOT include the newline in the message!
     * @return True when message successfully sent, false on error.
     */
    private boolean sendRequestToServer(String request) {
        // TODO - implement this method
        // Hint: What can go wrong? Several things:
        // * Connection closed by remote host (server shutdown)
        // * Internet connection lost, timeout in transmission
        // * Connection not opened.
        // * What if the request is null or empty?
     
        try {
            OutputStream out = this.socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            writer.println(request);
            return true;
            
        } 
        catch (SocketException sex){
            System.out.println("My Socket Exception " + sex);
            return false;
        }
        catch (IOException ioex) {
            System.out.println("SOME ERROR in sendRequestToServer" + ioex);
            return false;
        } 
    }

    /**
     * Wait for one response from the remote server.
     *
     * @return The response received from the server, null on error. The newline character is stripped away
     * (not included in the returned value).
     */
    private String readResponseFromServer() {
        // TODO - implement this method
        // Similarly to other methods, exception can happen while trying to read the input stream of the TCP Socket
        
        try{
            InputStream in = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String oneResponseLine;
            
            do {                
                oneResponseLine = reader.readLine();
                if(oneResponseLine != null)
                {
                    return oneResponseLine;
                }
            } while (oneResponseLine != null);
            

        }
        catch(Exception e){
            System.out.println("Eitthvað að í readResponseFromServer " + e);
        }
        
        
        
        
            
            
        return null;
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message) {
        String threadId = "THREAD #" + Thread.currentThread().getId() + ": ";
        System.out.println(threadId + message);
    }
}
