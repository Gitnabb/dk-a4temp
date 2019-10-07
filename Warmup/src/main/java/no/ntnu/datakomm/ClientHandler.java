/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author sigurdurhj80
 */
class ClientHandler extends Thread
{
    private final Socket clientSocket;
    
    public ClientHandler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run()
    {
        try{
            InputStreamReader reader = new InputStreamReader(this.clientSocket.getInputStream());
            BufferedReader bufReader = new BufferedReader(reader);

            String clientInput = bufReader.readLine();
            System.out.println("Client: " + clientInput);

            String response = "Hei from server";

            PrintWriter writer = new PrintWriter(this.clientSocket.getOutputStream(), true);
            writer.println(response);

            if(clientInput.equalsIgnoreCase("game over"))
            {
                // close client socket
                this.clientSocket.close();
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
