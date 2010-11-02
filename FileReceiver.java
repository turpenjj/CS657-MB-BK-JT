/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;

/**
 *
 * @author Matt
 */
public class FileReceiver {

    public static void main (String[] args) throws Exception
    {
        short RECEIVER_SOCKET = 9876;
        int BUFFER_SIZE = 1024;

        System.out.println("What the bung");
        try {
            DatagramSocket receiverSocket = new DatagramSocket(RECEIVER_SOCKET);

            byte[] receiveData = new byte[BUFFER_SIZE];
            byte[] sendData = new byte[BUFFER_SIZE];

            while ( true ) {
                receiveData = new byte[BUFFER_SIZE];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                System.out.println("Waiting for datagram packet");

                receiverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                byte[] myData = receivePacket.getData();
                int length = receivePacket.getLength();

                ProcessPacket(myData, length);

                System.out.println("Received packet from " + IPAddress + ":" + port);
                System.out.println("This is our data: " + receivePacket.toString());
            }
        } catch (SocketException ex) {
            System.out.println("UDP port unavailable");
            System.exit(1);
        }
    }

    private static boolean WriteTemporaryFile( String filename, byte[] data, int length ) {
        File outFile;

        outFile = new File(filename);
        try {
            FileOutputStream file_output = new FileOutputStream(outFile);
            DataOutputStream data_output = new DataOutputStream(file_output);

            data_output.write(data);

            file_output.close();

        } catch (IOException e) {
            System.out.println("IO Exception: " + e);
        }

        return true;
    }

    private static void ProcessPacket( byte[] data, int length ) {
        String myString = new String(data);
        System.out.println("Received: " + myString);
        String tempFilename = "filename.tmp.sha1hash";
        int dataLength = 10;
        byte[] dataToWrite = data;

        WriteTemporaryFile(tempFilename, dataToWrite, dataLength);
    }
}
