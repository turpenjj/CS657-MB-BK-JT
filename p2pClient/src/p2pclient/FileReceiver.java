/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.net.*;

/**
 *
 * @author Matt
 */
public class FileReceiver extends Util {

    public static void main (String[] args) throws Exception
    {
        short RECEIVER_SOCKET = 9876;
        int BUFFER_SIZE = 1280;

        try {
            DatagramSocket receiverSocket = new DatagramSocket(RECEIVER_SOCKET);

            byte[] receiveData = new byte[BUFFER_SIZE];
            byte[] sendData = new byte[BUFFER_SIZE];

            while ( true ) {
                receiveData = new byte[BUFFER_SIZE];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                Util.DebugPrint(DbgSub.FILE_RECEIVER, "Waiting for datagram packet");

                receiverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                byte[] myData = receivePacket.getData();
                int length = receivePacket.getLength();

                ProcessPacket(myData, length);

                Util.DebugPrint(DbgSub.FILE_RECEIVER, "Received packet from " + IPAddress + ":" + port);
            }
        } catch (SocketException ex) {
            Util.DebugPrint(DbgSub.FILE_RECEIVER, "UDP port unavailable");
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
            Util.DebugPrint(DbgSub.FILE_RECEIVER, "IO Exception: " + e);
        }

        return true;
    }

    private static void ProcessPacket( byte[] data, int length ) {
        String myString = new String(data);

//        Util.DebugPrint(DbgSub.FILE_RECEIVER, "Received: " + myString);
//        String tempFilename = "ReceivedFile.tmp.";
//        FileChunk recvChunk = new FileChunk(tempFilename, 0);
//        byte[] temp = new byte[4];
//        byte[] dataHash = new byte[20];
//        int dataLength = length - 28;
////        recvChunk.SetOffset(ByteArrayToInt(data));
////        System.arraycopy(data, 0, temp, 0, 4);
////        recvChunk.SetOffset(ByteArrayToInt(temp));
////        System.arraycopy(data, 4, temp, 0, 4);
////        recvChunk.SetLength(ByteArrayToInt(temp));
////        System.arraycopy(data, 8, dataHash, 0, dataHash.length);
//
//        Util.DebugPrint(DbgSub.FILE_RECEIVER, "buffer length: " + data.length + " vs " + length);
//        Util.DebugPrint(DbgSub.FILE_RECEIVER, "Received data: " + recvChunk.ConvertToHex(data));
////        Util.DebugPrint(DbgSub.FILE_RECEIVER, "Received data length: " + dataLength + " vs expected " + recvChunk.GetLength());
////        Util.DebugPrint(DbgSub.FILE_RECEIVER, "Data offset: " + recvChunk.GetOffset());
//        Util.DebugPrint(DbgSub.FILE_RECEIVER, "Hash: " + recvChunk.ConvertToHex(dataHash));
//
//        byte[] dataToWrite = new byte[dataLength];
//        System.arraycopy(data, 28, dataToWrite, 0, dataLength);
//
//        recvChunk.SetChunk(dataToWrite, dataLength);
//
//        tempFilename = tempFilename.concat(recvChunk.GetHashString());
//
//        WriteTemporaryFile(tempFilename, dataToWrite, dataLength);
    }
}
