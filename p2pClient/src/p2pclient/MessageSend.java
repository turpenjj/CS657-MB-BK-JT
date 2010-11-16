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
public class MessageSend {
    private static int MAX_PACKET_DATA_SIZE = 1480 - MessageSend.MAX_PACKET_DATA_SIZE; // Ethernet MTU of 1500 minus 20 bytes for UDP header

    /*
     * Handles dividing a message and its data into individual packets and sending them to a peer.
     */
    public void SendCommunication(Peer peer, PacketType packetType, int sessionID, byte[] sendData) {
        // create the sending socket
        // loop through the data and call SendPacket() for each chunk
        try {
            DatagramSocket sendingSocket = new DatagramSocket();
            PacketHeader packetHeader;
            int currentOffset = 0;
            int currentPacket = 1;
            int totalSize = sendData.length;
            byte[] packetDataBuffer;
            int dataSize;
            
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "Message being sent: Session ID = " + sessionID + "; " + packetType + "; Total Size = " + totalSize);
            while (currentOffset < totalSize) {
                packetHeader = new PacketHeader(sessionID, packetType, totalSize, currentOffset);
                
                if (totalSize - currentOffset > MessageSend.MAX_PACKET_DATA_SIZE) {
                    dataSize = MessageSend.MAX_PACKET_DATA_SIZE;
                } else {
                    dataSize = totalSize - currentOffset;
                }
                packetDataBuffer = new byte[dataSize];
                
                System.arraycopy(sendData, currentOffset, packetDataBuffer, 0, dataSize);
                SendPacket(sendingSocket, peer, packetHeader, packetDataBuffer);

                currentOffset += dataSize;
            }
        } catch ( SocketException e ) {
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "SendCommunication Socket error " + e);
        }
    }

    /*
     * Creates and sends an individual packet out a socket.
     */
    private void SendPacket(DatagramSocket sendingSocket, Peer peer, PacketHeader packetHeader, byte[] packetData) {
        // create the packet
        // send the packet
        byte[] sendBuffer = new byte[packetData.length + packetHeader.GetBytes().length];
        System.arraycopy(packetHeader.GetBytes(), 0, sendBuffer, 0, packetHeader.GetBytes().length);
        System.arraycopy(packetData, 0, sendBuffer, packetHeader.GetBytes().length, packetData.length);
        try {
            InetAddress ipAddress = peer.clientIp;
            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, ipAddress, peer.listeningPort);
            sendingSocket.send(packet);
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "Sent out packet to " + ipAddress + " on port " + peer.listeningPort);
            Util.DebugPrint(DbgSub.MESSAGE_SEND, Util.ConvertToHex(sendBuffer));
        } catch ( IOException e) {
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "SocketSend encountered an error: " + e);
        }
    }

}
