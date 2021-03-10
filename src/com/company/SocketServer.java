package com.company;

import com.example.wirelessdatatransfer.FileDetails;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class SocketServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public SocketServer(int port )
    {
        this.port = port;
    }


    public void startServer()
    {
        try
        {
            serverSocket = new ServerSocket( port );
            this.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopServer()
    {
        running = false;
        this.interrupt();
    }

    @Override
    public void run()
    {
        running = true;
        while( running )
        {
            try
            {
                System.out.println( "Listening for a connection" );

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler( socket );
                requestHandler.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main( String[] args )
    {
        int port = 4000;
        System.out.println( "Start server on port: " + port );

        SocketServer server = new SocketServer( port );
        server.startServer();

        // Automatically shutdown in 1 minute
        try
        {
            Thread.sleep( 60000 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        server.stopServer();
    }


}



class RequestHandler extends Thread
{
    private Socket socket;
    RequestHandler( Socket socket )
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println( "Received a connection" );

            System.out.println("Getting details from Client...");
            ObjectInputStream getDetails = new ObjectInputStream(socket.getInputStream());
            FileDetails details = (FileDetails) getDetails.readObject();
            System.out.println("Now receiving file...");
            // Storing file name and sizes

            String fileName = details.getName();
            long fileSize = details.getSize();
            System.out.println("File Name : " + fileName);
            byte[] data = new byte[1024]; // Here you can increase the size also which will receive it faster
            InputStream in = socket.getInputStream();
            FileOutputStream path = new FileOutputStream("C:\\DataTransferServerFiles\\" + fileName);
            BufferedOutputStream out = new BufferedOutputStream(path);
            int remainent;
            int count;
            int sum = 0;

            while ((count = in.read(data)) > 0) {
                sum += count;
//                remainent = length - sum;
//                int percent = (100 *sum) / length;
//                System.out.println("Read: " +  count + " - Received: " + sum + "/" + length + " - Remainent: " + remainent + " -- " + percent + "%");
                out.write(data, 0, count);
                out.flush();
                System.out.println("Data received : " + sum + " / " + fileSize);
//                if (remainent == 0) break;
            }
            out.flush();
            System.out.println("File Received...");
            out.close();
            in.close();
            socket.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}