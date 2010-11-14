/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;
import java.io.*;
import java.net.*;
import java.util.Calendar;

/**
 *
 * @author Matt
 */
public class Util {
    public static String ConvertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();

        for ( int i = 0; i < data.length; i++ ) {
            int halfbyte =  (data[i] >>> 4) & 0x0F;
            int two_halves = 0;
            do {
                if ( (0 <= halfbyte) && (halfbyte <= 9) ) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halves++ < 1);
        }
        return buf.toString();
    }

    public static byte[] IntToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)(value)};
    }

    public static int ByteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public static int[] ByteArrayToIntArray(byte[] b) {
        if (b.length % 4 != 0) {
            return null;
        }
        int[] intArray = new int[b.length/4];

        for ( int i = 0; i < b.length/4; i++ ) {
            int byteArrayIndex = i*4;
            intArray[i] = (b[byteArrayIndex] << 24)
                    + ((b[byteArrayIndex+1] & 0xFF) << 16)
                    + ((b[byteArrayIndex+2] & 0xFF) << 8)
                    + (b[byteArrayIndex+3] & 0xFF);
        }

        return intArray;
    }

    public static boolean SendPacket(Peer peer, byte[] payload) {
        int SOCKET_TIMEOUT = 15000;
        try {
           DatagramSocket sendingSocket = new DatagramSocket();
           InetAddress IPAddress = InetAddress.getByAddress(IntToByteArray(peer.clientIp));
           int port = peer.listeningPort;

           DatagramPacket packetToSend = new DatagramPacket(payload, payload.length, IPAddress, port);

           sendingSocket.setSoTimeout(SOCKET_TIMEOUT);
           sendingSocket.send(packetToSend);
           sendingSocket.close();
        } catch ( IOException e ) {
            System.out.println("SendPacket Error: " + e);
            return false;
        }
        return true;
    }

    public static int ReceivePacket(Peer peer, int receivingPort, byte[] receivedData) {
        int SOCKET_TIMEOUT = 15000;
        int lengthReceived = 0;
        int MAX_PACKET_SIZE = 1500;
        receivedData = new byte[MAX_PACKET_SIZE];
        
        try {
            DatagramSocket receivingSocket = new DatagramSocket(receivingPort);
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);

            receivingSocket.setSoTimeout(SOCKET_TIMEOUT);
            receivingSocket.receive(receivedPacket);
            InetAddress senderIP = receivedPacket.getAddress();

            peer.clientIp = ByteArrayToInt(senderIP.getAddress());
            lengthReceived = receivedPacket.getLength();
        } catch ( IOException e ) {
            System.out.println("ReceivePacket Error: " + e);
        }
        return lengthReceived;
    }

    public static int ReceiveCommunication(Peer peer, int receivingPort, byte[] receivedData) {
        int bytesRead;
        int totalBytesReceived = 0;
        int sessionID = -1;
        byte[] rawReceivedData;
        byte[] tempArray = new byte[16];
        int[] rawPacketHeader;
        PacketHeader packetHeader;

//        do {
//            rawReceivedData = null;
//            bytesRead = ReceivePacket(peer, receivingPort, rawReceivedData);
//            if ( bytesRead < 16 ) {
//                return 0;
//            }
//            System.arraycopy(rawReceivedData, 0, tempArray, 0, 16);
//            rawPacketHeader = ByteArrayToIntArray(tempArray);
//            packetHeader = new PacketHeader(rawPacketHeader[0], rawPacketHeader[1], rawPacketHeader[2], rawPacketHeader[3]);
//
//            if ( sessionID == -1 ) {
//                sessionID = packetHeader.sessionID;
//                receivedData = new byte[packetHeader.totalSize];
//            } else if ( sessionID != packetHeader.sessionID ) {
//                System.out.println("Received packet for the wrong session");
//                return 0;
//            }
//            System.arraycopy(rawReceivedData, 16, receivedData, packetHeader.offset, bytesRead - 16);
//            totalBytesReceived += bytesRead - 16;
//        } while ( totalBytesReceived < packetHeader.totalSize ) ;
        return totalBytesReceived;
    }

    /*
     * Handles dividing a message and its data into individual packets and sending them to a peer.
     */
    public void SendCommunication(Peer peer, PacketType packetType, int sessionID, byte[] sendData) {
        // create the sending socket
        // loop through the data and call SendPacket() for each chunk
    }

    /*
     * Creates and sends an individual packet out a socket.
     */
    private void SendPacket(DatagramSocket sendingSocket, PacketHeader packetHeader, byte[] packetData) {
        // create the packet
        // send the packet
    }

    public static long GetCurrentTime() {
        return (Calendar.getInstance()).getTimeInMillis();
    }
}