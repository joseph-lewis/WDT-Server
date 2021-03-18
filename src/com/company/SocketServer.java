package com.company;

import com.example.wirelessdatatransfer.FileDetails;

import java.io.*;
import java.net.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class SocketServer extends Thread {
    private static final String CSV_FILE_NAME = "devices.csv";
    private ServerSocket serverSocket;
    private boolean running = false;
    private int finalPORT;
    private int port;

    public SocketServer(int port )
    {
        this.port = port;
    }


    public void startServer(int port)
    {
        try
        {
            serverSocket = new ServerSocket( port );
            finalPORT = port;
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
                if(finalPORT == 4000){
                    RequestHandler requestHandler = new RequestHandler( socket );
                    requestHandler.start();
                }
                else{
                    AddDevice addDevice = new AddDevice(socket);
                    addDevice.start();
                }


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main( String[] args ) throws IOException {
        int port1 = 4000;
        int port2 = 3000;
        System.out.println( "Start server on port: " + port1 );

        initializeCSV();

        SocketServer dataServer = new SocketServer( port1 );
        SocketServer infoServer = new SocketServer( port2 );
        dataServer.startServer(port1);
        infoServer.startServer(port2);
        // Automaticsally shutdown in 1 minute
        try
        {
            Thread.sleep( 60000 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        infoServer.stopServer();
        dataServer.stopServer();
    }

    private static void initializeCSV() throws IOException {
        BufferedReader csvReader = null;
        try {
            csvReader = new BufferedReader(new FileReader(CSV_FILE_NAME));
            csvReader.close();
        } catch (FileNotFoundException e) {
            PrintWriter writer = new PrintWriter(new File(CSV_FILE_NAME));
            writer.close();
            System.out.println("done!");
        }
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
            obtainIP(socket);
            ObjectInputStream getDetails = new ObjectInputStream(socket.getInputStream());
            FileDetails details = (FileDetails) getDetails.readObject();
            System.out.println("Now receiving file...");
            // Storing file name and sizes

            String fileName = details.getName();
            long fileSize = details.getSize();
            System.out.println("File Name : " + fileName);
            byte[] data = new byte[1024]; // Here you can increase the size also which will receive it faster
            InputStream in = socket.getInputStream();
//            home/ubuntu/WDT-Server/data/
            String filePath = System.getProperty("user.dir");
            FileOutputStream path = new FileOutputStream(filePath+ "\\" + fileName);
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
                System.out.println("Data received 4: " + sum + " / " + fileSize);
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

    private void obtainIP(Socket socket) {
        SocketAddress socketAddress = socket.getRemoteSocketAddress();

        if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress)socketAddress).getAddress();
            if (inetAddress instanceof Inet4Address)
                System.out.println("IPv4: " + inetAddress);
            else if (inetAddress instanceof Inet6Address)
                System.out.println("IPv6: " + inetAddress);
            else
                System.err.println("Not an IP address.");
        } else {
            System.err.println("Not an internet protocol socket.");
        }

    }
}