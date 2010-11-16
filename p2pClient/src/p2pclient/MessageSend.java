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

    /*
     * Handles dividing a message and its data into individual packets and sending them to a peer.
     */
    public void SendCommunication(Peer peer, PacketType packetType, int sessionID, byte[] sendData) {
        // create the sending socket
        // loop through the data and call SendPacket() for each chunk
        try {
            DatagramSocket sendingSocket = new DatagramSocket();
            PacketHeader packetHeader = new PacketHeader(sessionID, packetType, sendData.length, 0);
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "Sending out packet with sessionID " + sessionID);
            SendPacket(sendingSocket, peer, packetHeader, sendData);
        } catch ( SocketException e ) {
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "SendCommunication Socket error" + e);
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
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "Sent out packet to " + ipAddress + " on port " + peer.listeningPort);;
        } catch ( IOException e) {
            Util.DebugPrint(DbgSub.MESSAGE_SEND, "SocketSend encountered an error: " + e);
        }
    }

}
