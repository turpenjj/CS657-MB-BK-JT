package p2pclient;

import java.io.*;
import java.net.*;

/**
 * This is a utility class for receiving messages that arrive in one or more UDP
 * packets. It has a listening thread and listening socket for each instance.
 *
 * @author Jeremy
 */
public class MessageReceive extends Util implements Runnable {
    private Thread runner;
    private volatile boolean stop = false;
    private DatagramSocket listeningSocket;
    private MessageBuffer[] messageBuffers;
    private PacketHeader[] headers;
    private byte[][] packetData;
    private PacketType[] acceptedPacketTypes;
    private int SOCKET_TIMEOUT_MS = 15000;
    private int MAX_PACKET_SIZE = 1500;
    private int BASE_SOCKET_PORT = 2000;
    private int MAX_PORT_NUMBER = 65535;
    public int listeningPort;

    MessageReceive(int listeningPort, PacketType[] acceptedPacketTypes, boolean permanent) {
        this.acceptedPacketTypes = acceptedPacketTypes;
        try {
            this.listeningSocket = new DatagramSocket(listeningPort);
            if ( !permanent ) {
                this.listeningSocket.setSoTimeout(this.SOCKET_TIMEOUT_MS);
            }
        } catch ( IOException e ) {
            System.out.println("Receieved socket error: " + e);
        }
    }

    MessageReceive(PacketType[] acceptedPacketTypes) {
        this.acceptedPacketTypes = acceptedPacketTypes;
        this.listeningPort = BASE_SOCKET_PORT;
        while ( listeningPort < MAX_PORT_NUMBER )  {
            try {
                this.listeningSocket = new DatagramSocket(this.listeningPort);
                System.out.println("Opened a socket on port " + this.listeningPort);
                this.listeningSocket.setSoTimeout(this.SOCKET_TIMEOUT_MS);
                break;
            } catch ( IOException e ) {
                this.listeningPort += 1;
            }
        }
    }

    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void Stop() {
        this.listeningSocket.close();
        stop = true;
    }

    public void run() {
        System.out.println("Started a Message Receive thread " + this.listeningSocket.getLocalPort());

        /* Outline:
         * Call ReceiveCommunication in a loop. Don't worry about timeouts. When a full message is received, call ProcessQuery().
         */
        while ( !stop ) {
            Peer[] peer = new Peer[1];
            PacketHeader[] packetHeader = new PacketHeader[1];
            byte[][] packetInData = new byte[1][];
            byte[] receivedPacketData = null;
            MessageBuffer messageBuff;

            if ( ReceivePacket(peer, packetHeader, packetInData) != -1 ) {
                receivedPacketData = packetInData[0];
                boolean accept = false;
                for (PacketType type : this.acceptedPacketTypes) {
//                    int[] temp = new int[1];
//                    System.out.println("String: " + Util.ExtractNullTerminatedString(receivedPacketData, 4, temp));
                    if (packetHeader[0].packetType == type) {
                        accept = true;
                    } else {
                    }
                }
                if ( accept ) {
                    System.out.println("Accepting this packet");
                    if ( (messageBuff = FindMessage(packetHeader[0].sessionID)) != null ) {
                        messageBuff.AddDataToMessage(packetHeader[0], receivedPacketData);
                    } else {
                        messageBuff = new MessageBuffer(peer[0], packetHeader[0]);
                        messageBuff.AddDataToMessage(packetHeader[0], receivedPacketData);
                        AddToMessageList(messageBuff);
                    }
                } else {
                    // TODO: discard received packet data
                    System.out.println("Rejecting this packet");
                }
            }
        }
    }

    /**
     * Finds the first complete message matching the filter criteria.
     *
     * @param filterSessionID SessionID to find message for (0 for don't care)
     * @param filterPacketType Array of acceptable packet types (ignored if filterSessionID is not 0)
     * @param peer[out]
     * @param packetType[out]
     * @param sessionID[out]
     * @param packetData[out]
     *
     * @return true if a message was found and output parameters are populated, false otherwise
     */
    public boolean GetMessage(int filterSessionID, PacketType[] filterPacketType, Peer[] peer, PacketType[] packetType, int[] sessionID, byte[][] packetData) {
        MessageBuffer message;

        //Need to yield to allow this.messageBuffers to be updated....
        Thread.currentThread().yield();

        if (filterSessionID != 0) {
            if ((message = this.FindMessage(filterSessionID)) != null) {
                if (message.IsMessageComplete(peer, packetType, sessionID, packetData)) {
                    // TODO: remove message from this.messageBuffers
                    RemoveMessageFromList(message);
                    return true;
                }
            }
        } else {
            if (this.messageBuffers != null ) {
                for (MessageBuffer loopMessage : this.messageBuffers) {
                    if (loopMessage.IsMessageComplete(peer, packetType, sessionID, packetData)) {
                        for (PacketType type : this.acceptedPacketTypes) {
                            System.out.println("Checking " + type + " vs " + packetType[0]);
                            if (packetType != null && packetType[0] == type) {
                                System.out.println("We have a match!");
                                // TODO: remove message from this.messageBuffers
                                RemoveMessageFromList(loopMessage);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private void AddToMessageList(MessageBuffer messageToAdd) {
        if ( this.messageBuffers == null ) {
            this.messageBuffers = new MessageBuffer[1];
        } else {
            MessageBuffer[] tempList = new MessageBuffer[this.messageBuffers.length + 1];
            System.arraycopy(this.messageBuffers, 0, tempList, 0, this.messageBuffers.length);
            this.messageBuffers = tempList;
        }
        this.messageBuffers[this.messageBuffers.length - 1] = messageToAdd;
    }

    private void RemoveMessageFromList(MessageBuffer messageToRemove) {
        if ( this.messageBuffers.length == 1 ) {
            this.messageBuffers = null;
        } else {
            int messageIndex = FindMessageIndex(messageToRemove);
            if ( messageIndex != -1 ) {
                MessageBuffer[] tempList = new MessageBuffer[this.messageBuffers.length - 1];
                System.arraycopy(this.messageBuffers, 0, tempList, 0, messageIndex);
                System.arraycopy(this.messageBuffers, messageIndex + 1, tempList, messageIndex, tempList.length - messageIndex);
                this.messageBuffers = tempList;
            }
        }
    }

    private int FindMessageIndex(MessageBuffer messageToFind) {
        for (int i = 0; this.messageBuffers != null && i < this.messageBuffers.length; i++ ) {
            if ( this.messageBuffers[i].sessionID == messageToFind.sessionID ) {
                return i;
            }
        }
        return -1;
    }

    private MessageBuffer FindMessage(int sessionID) {
        long currentTimeInMsec = GetCurrentTime();

        if ( this.messageBuffers == null ) {
            return null;
        }
        for (MessageBuffer message : this.messageBuffers) {
            if (message.sessionID == sessionID) {
                return message;
            } else if (message.IsMessageTimedOut(currentTimeInMsec)) {
                // TODO: remove from this.messageBuffers
                // TODO: destroy message
            }
        }
        return null;
    }

    /**
     * Read a packet from a
     * @param listeningSocket
     * @param peer[out]
     * @param packetHeader[out]
     * @param packetData[out]
     *
     * @return > 0 if packet data received, 0 on socket timeout, -1 on socket error
     */
    private int ReceivePacket(Peer[] peer, PacketHeader[] packetHeader, byte[][] packetData) {
        byte[] rawData = new byte[this.MAX_PACKET_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(rawData, rawData.length);

        try {
            this.listeningSocket.receive(receivedPacket);
            InetAddress senderIP = receivedPacket.getAddress();
            peer[0] = new Peer(senderIP, 0);

            byte[] data = receivedPacket.getData();
            packetHeader[0] = new PacketHeader();
            packetHeader[0].ImportHeader(data);
            packetData[0] = new byte[receivedPacket.getLength() - 16];
            System.arraycopy(data, 16, packetData[0], 0, packetData[0].length);
            return 0;
        } catch ( IOException e ) {
            System.out.println("ReceivePacket error: " + e);

            return -1;
        }
    }

//    public static int ReceivePacket(Peer peer, int receivingPort, byte[] receivedData) {
//        int SOCKET_TIMEOUT = 15000;
//        int lengthReceived = 0;
//        int MAX_PACKET_SIZE = 1500;
//        receivedData = new byte[MAX_PACKET_SIZE];
//
//        try {
//            DatagramSocket receivingSocket = new DatagramSocket(receivingPort);
//            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
//
//            receivingSocket.setSoTimeout(SOCKET_TIMEOUT);
//            receivingSocket.receive(receivedPacket);
//            InetAddress senderIP = receivedPacket.getAddress();
//
//            peer.clientIp = ByteArrayToInt(senderIP.getAddress());
//            lengthReceived = receivedPacket.getLength();
//        } catch ( IOException e ) {
//            System.out.println("ReceivePacket Error: " + e);
//        }
//        return lengthReceived;
//    }
//
//    public static int ReceiveCommunication(Peer peer, int receivingPort, byte[] receivedData) {
//        int bytesRead;
//        int totalBytesReceived = 0;
//        int sessionID = -1;
//        byte[] rawReceivedData;
//        byte[] tempArray = new byte[16];
//        int[] rawPacketHeader;
//        PacketHeader packetHeader;
//
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
//        return totalBytesReceived;
//    }

}
