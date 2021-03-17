package com.company;

import java.io.*;
import java.net.Socket;

public class AddDevice extends Thread{
    private Socket socket;
    AddDevice( Socket socket )
    {
        this.socket = socket;
    }

//    @Override
    public void run()
    {
        try
        {
            System.out.println( "Received a connection" );

            System.out.println("Getting details from Client...");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String message = dataInputStream.readUTF();
            System.out.println("The message sent from the socket was: " + message);

            System.out.println("Closing sockets.");
            socket.close();


        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
