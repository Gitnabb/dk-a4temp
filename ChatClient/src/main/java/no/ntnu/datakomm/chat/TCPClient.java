package no.ntnu.datakomm.chat;

import jdk.internal.util.xml.impl.Input;
import jdk.nashorn.internal.runtime.ECMAException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;


public class TCPClient {
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Socket connection;
    private String lastError = null;

    private final List<ChatListener> listeners = new LinkedList<>();

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    public boolean connect(String host, int port) {
        try {
            this.connection = new Socket(host, port);
            InputStream in = this.connection.getInputStream();
            OutputStream out = this.connection.getOutputStream();
            this.toServer = new PrintWriter(out, true);
            this.fromServer = new BufferedReader(new InputStreamReader(in));
            return true;
        } catch (UnknownHostException e) {
            this.lastError = "Unknown host";
            System.out.println(this.lastError);
            return false;
        } catch (IOException e) {
            this.lastError = "I/O Error";
            System.out.println(this.lastError);
            return false;
        }
    }


    /**
     * Close the socket. This method must be synchronized, because several
     * threads may try to call it. For example: When "Disconnect" button is
     * pressed in the GUI thread, the connection will get closed. Meanwhile, the
     * background thread trying to read server's response will get error in the
     * input stream and may try to call this method when the socket is already
     * in the process of being closed. with "synchronized" keyword we make sure
     * that no two threads call this method in parallel.
     */
    public synchronized void disconnect() {
            if (this.connection != null) {
                System.out.println("Disconnecting...");

                try {
                    this.toServer.close();
                    this.fromServer.close();
                    this.connection.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e
                            .getMessage());
                    this.lastError = e.getMessage();
                    this.connection = null;
                }
            } else {
                System.out.println("No connection to close");
            }
            System.out.println("Disconnected");
            this.connection = null;
        }


    /**
     * @return true if the connection is active (opened), false if not.
     */
    public boolean isConnectionActive() {
        return connection != null;
    }

    /**
     * Send a command to server.
     *
     * @param cmd A command. It should include the command word and optional attributes, according to the protocol.
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {

        if (this.connection != null) {
            System.out.println("> " + cmd);
            this.toServer.println(cmd);
            return true;
        }
        System.out.println("No connection");
        return false;
    }

    /**
     * Send a public message to all the recipients.
     *
     * @param message Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPublicMessage(String message) {

        // Hint: update lastError if you want to store the reason for the error.
        return sendTextMessage("msg", null, message);
    }

    /**
     * Send a login request to the chat server.
     *
     * @param username Username to use
     */
    public void tryLogin(String username) {

        sendCommand("login" + username);

    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList() {
        // TODO Step 5: implement this method
        // Hint: Use Wireshark and the provided chat client reference app to find out what commands the
        // client and server exchange for user listing.
    }

    private boolean sendTextMessage(String cmd, String recipient, String message) {
        if (!isValidMessage(message)) {
            return false;
        }
        String cmdToSend = cmd + " ";
        if (recipient != null && cmdToSend.length() > 0) {
            cmdToSend = cmdToSend + recipient + " ";
        }
        cmdToSend = cmdToSend + message;
        sendCommand(cmdToSend);
        return true;
    }


    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message   Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPrivateMessage(String recipient, String message) {
        // TODO Step 6: Implement this method

        boolean messageSent = false;

        if (!isValidMessage(message)) {
            messageSent = false;
        }

        /*
        try{
            sendCommand(message);

        } catch (Exception e){
            System.out.println("There was en error sending message" + e.getMessage());
            this.lastError = e.getMessage();
        }

        // Hint: update lastError if you want to store the reason for the error. */

        sendCommand(message);

        if(!sendCommand(message)){
            System.out.println("Message not sent");
            this.lastError = "Message not sent";
        }

        return messageSent;

    }

    /**
     * Check if message is valid
     * @param message
     * @return
     */

    private boolean isValidMessage(String message) {
        if (message.indexOf('\n') >= 0) {
            this.lastError = "Message contains newline, ignored";
            System.out.println(this.lastError);
            return false;
        }
        return true;
    }


    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands() {
        // TODO Step 8: Implement this method
        // Hint: Reuse sendCommand() method
    }


    /**
     * Wait for chat server's response, and try catch IO exception.
     * If exception is found disconnect server and close socket.
     * @return one line of text (one command) received from the server
     */
    private String waitServerResponse() {
        String output = null;
        try {
            output = this.fromServer.readLine();
            if (output != null) {
                System.out.println("<<< " + output);
            }
            else {
                disconnect();
            }
        } catch (IOException ex) {
            System.out.println("Error while reading server response, socket seems to be closed");
            this.lastError = "Server closed socket";
            disconnect();
            onDisconnect();
        }
        return output;
    }

    /**
     * Get the last error message
     *
     * @return Error message or "" if there has been no error
     */
    public String getLastError() {
        if (lastError != null) {
            return lastError;
        } else {
            return "";
        }
    }

    /**
     * Start listening for incoming commands from the server in a new CPU thread.
     */
    public void startListenThread() {
        // Call parseIncomingCommands() in the new thread.
        Thread t = new Thread(() -> {
            parseIncomingCommands();
        });
        t.start();
    }

    /**
     * Read incoming messages one by one, generate events for the listeners. A loop that runs until
     * the connection is closed.
     */
    private void parseIncomingCommands() {
        while (isConnectionActive()) {
            String line = waitServerResponse();
            if (line != null && line.length() > 0) {
                boolean priv;
                String params, cmd;
                int spacePos = line.indexOf(' ');
                if (spacePos >= 0) {
                    cmd = line.substring(0, spacePos);
                    params = line.substring(spacePos + 1);
                } else {
                    cmd = line;
                    params = " ";
                }
                switch (cmd) {
                    case "loginok":
                        onLoginResult(true, null);

                    case "loginerr":
                        onLoginResult(false, params);

                    case "msg":
                    case "privmsg":
                        priv = cmd.equals("privmsg");
                        spacePos = params.indexOf(' ');
                        if (spacePos > 0) {
                            String sender = params.substring(0, spacePos);
                            String msg = params.substring(spacePos + 1);
                            onMsgReceived(priv, sender, msg);
                        }
                    case "msgerr":
                        onMsgError(params);

                    case "cmderr":
                        onCmdError(params);

                    case "users":
                        onUsersList(params.split(" "));

                    case "supported":
                        onSupported(params.split(" "));

                }
            }
        }
    }


    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    public void addListener(ChatListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister an event listener
     *
     * @param listener
     */
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following methods are all event-notificators - notify all the listeners about a specific event.
    // By "event" here we mean "information received from the chat server".
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Notify listeners that login operation is complete (either with success or
     * failure)
     *
     * @param success When true, login successful. When false, it failed
     * @param errMsg  Error message if any
     */
    private void onLoginResult(boolean success, String errMsg) {
        for (ChatListener l : listeners) {
            l.onLoginResult(success, errMsg);
        }
    }

    /**
     * Notify listeners that socket was closed by the remote end (server or
     * Internet error)
     */
    private void onDisconnect() {
        for (ChatListener l : this.listeners)
            l.onDisconnect();
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        for (ChatListener l: this.listeners)
            l.onUserList(users);
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv   When true, this is a private message
     * @param sender Username of the sender
     * @param text   Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        TextMessage msg = new TextMessage(sender, priv, text);
        for (ChatListener l : this.listeners) {
            l.onMessageReceived(msg);
        }
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(String errMsg) {
        for (ChatListener l: this.listeners)
            l.onMessageError(errMsg);
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg Error message
     */
    private void onCmdError(String errMsg) {
        for (ChatListener l: this.listeners)
            l.onCommandError(errMsg);
    }


    /**
     * Notify listeners that a help response (supported commands) was received
     * from the server
     *
     * @param commands Commands supported by the server
     */
    private void onSupported(String[] commands) {
        for (ChatListener l: this.listeners)
            l.onSupportedCommands(commands);
    }

}
