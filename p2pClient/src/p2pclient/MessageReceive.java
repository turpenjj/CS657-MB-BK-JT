package p2pclient;

import java.io.*;
import java.net.*;

/**
 * This is a utility class for receiving messages that arrive in one or more UDP
 * packets. It has a listening thread and listening socket for each instance.
 *
 * When instantiated, the caller specifies whether or not this message receiver
 * is permanent.  If permanent, the thread sticks around indefinitely and the
 * socket will not timeout. If not permanent, the socket can timeout.  The
 * thread will always stick around at least until a call is made to GetMessage()
 * If the socket timed out before the call to GetMessage(), the thread will
 * stop after that call.  If the call to GetMessage() returns what the caller
 * needs, the thread will automatically stop.  Otherwise, the thread stays alive
 * Once stopped, the thread cannot be restarted and the caller will need to form
 * a new MessageReceive thread.
 *
 * @author Matt
 */
public class MessageReceive implements Runnable {
    private Thread runner;
    private volatile boolean stop = false;
    private boolean permanent;
    private boolean stopOnGet;
    private DatagramSocket listeningSocket;
    private MessageBuffer[] messageBuffers;
    private PacketType[] acceptedPacketTypes;
    private int SOCKET_TIMEOUT_MS = 15000;
    private int MAX_PACKET_SIZE = 1500;
    private int BASE_SOCKET_PORT = 2000;
    private int MAX_PORT_NUMBER = 65535;
    public int listeningPort;

    MessageReceive(int listeningPort, PacketType[] acceptedPacketTypes, boolean permanent) {
        this.acceptedPacketTypes = acceptedPacketTypes;
        this.listeningPort = listeningPort;
        try {
            this.listeningSocket = new DatagramSocket(listeningPort);
            if ( !permanent ) {
                this.listeningSocket.setSoTimeout(this.SOCKET_TIMEOUT_MS);
                this.permanent = false;
            } else {
                this.permanent = true;
            }
        } catch ( IOException e ) {
             Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Receieved socket error: " + e);
        }
    }

    MessageReceive(PacketType[] acceptedPacketTypes, boolean permanent) {
        this.acceptedPacketTypes = acceptedPacketTypes;
        this.listeningPort = Util.GetRandomHighPort(new java.util.Random());
        while ( listeningPort < MAX_PORT_NUMBER )  {
            try {
                this.listeningSocket = new DatagramSocket(this.listeningPort);
                Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Opened a socket on port " + this.listeningPort);
                this.listeningSocket.setSoTimeout(this.SOCKET_TIMEOUT_MS);
                this.permanent = permanent;
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
        if (this.listeningSocket != null) {
            Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Started a Message Receive thread " + this.listeningSocket.getLocalPort());
        } else {
            Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Listening socket not created");
            return;
        }

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
                    if (packetHeader[0].packetType == type) {
                        accept = true;
                    } else {
                    }
                }
                if ( accept && receivedPacketData != null ) {
                    Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Accepting this packet");
                    Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, Util.ConvertToHex(receivedPacketData));
                    if ( (messageBuff = FindMessage(packetHeader[0].sessionID)) != null ) {
                        messageBuff.AddDataToMessage(packetHeader[0], receivedPacketData);
                    } else {
                        messageBuff = new MessageBuffer(peer[0], packetHeader[0]);
                        messageBuff.AddDataToMessage(packetHeader[0], receivedPacketData);
                        AddToMessageList(messageBuff);
                    }
                } else {
                    // TODO: discard received packet data
                    Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Rejecting this packet");
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
     *
     * @return Message data if there was a matching message, NULL otherwise
     */
    public synchronized byte[] GetMessage(int filterSessionID, PacketType[] filterPacketType, Peer[] peer, PacketType[] packetType, int[] sessionID) {
        MessageBuffer message;

        //Need to yield to allow this.messageBuffers to be updated....
        Thread.currentThread().yield();
        byte[] messageData = null;
        //If the socket timed out and this isn't a permanent listener, stop the thread after processing their GetMessage
        if ( stopOnGet ) {
            stop = true;
        }

        if (filterSessionID != 0) {
            if ((message = this.FindMessage(filterSessionID)) != null) {
                if ( (messageData = message.IsMessageComplete(peer, packetType, sessionID)) != null) {
                    // TODO: remove message from this.messageBuffers
                    RemoveMessageFromList(message);
                    if ( !permanent ) {
                        stop = true;
                    }
                    return messageData;
                }
            }
        } else {
            for ( int i = 0; this.messageBuffers != null && i < this.messageBuffers.length; i++ ) {
//            for (MessageBuffer loopMessage : this.messageBuffers) {
                MessageBuffer loopMessage = this.messageBuffers[i];
                if ( (messageData = loopMessage.IsMessageComplete(peer, packetType, sessionID)) != null ) {
                    for (PacketType type : this.acceptedPacketTypes) {
                        Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, "Checking " + type + " vs " + packetType[0]);
                        if (packetType != null && packetType[0] == type) {
                            // TODO: remove message from this.messageBuffers
                            RemoveMessageFromList(loopMessage);
                            if ( !permanent ) {
                                stop = true;
                            }
                            Thread.yield();
                            return messageData;
                        }
                    }
                }
            }
        }

        return null;
    }

    private synchronized void AddToMessageList(MessageBuffer messageToAdd) {
        if ( this.messageBuffers == null ) {
            this.messageBuffers = new MessageBuffer[1];
        } else {
            MessageBuffer[] tempList = new MessageBuffer[this.messageBuffers.length + 1];
            System.arraycopy(this.messageBuffers, 0, tempList, 0, this.messageBuffers.length);
            this.messageBuffers = tempList;
        }
        this.messageBuffers[this.messageBuffers.length - 1] = messageToAdd;
    }

    private synchronized MessageBuffer RemoveMessageFromList(MessageBuffer messageToRemove) {
        MessageBuffer removedMessage = null;
        if ( this.messageBuffers.length == 1 ) {
            removedMessage = this.messageBuffers[0];
            this.messageBuffers = null;
        } else {
            int messageIndex = FindMessageIndex(messageToRemove);
            if ( messageIndex != -1 ) {
                removedMessage = this.messageBuffers[messageIndex];
                MessageBuffer[] tempList = new MessageBuffer[this.messageBuffers.length - 1];
                System.arraycopy(this.messageBuffers, 0, tempList, 0, messageIndex);
                System.arraycopy(this.messageBuffers, messageIndex + 1, tempList, messageIndex, tempList.length - messageIndex);
                this.messageBuffers = tempList;
            }
        }
        return removedMessage;
    }

    private synchronized int FindMessageIndex(MessageBuffer messageToFind) {
        for (int i = 0; this.messageBuffers != null && i < this.messageBuffers.length; i++ ) {
            if ( this.messageBuffers[i].sessionID == messageToFind.sessionID ) {
                return i;
            }
        }
        return -1;
    }

    private synchronized MessageBuffer FindMessage(int sessionID) {
        long currentTimeInMsec = Util.GetCurrentTime();

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
            Util.DebugPrint(DbgSub.MESSAGE_RECEIVE, " " + senderIP);
            peer[0] = new Peer(senderIP, 0);

            byte[] data = receivedPacket.getData();
            packetHeader[0] = new PacketHeader();
            packetHeader[0].ImportHeader(data);
            packetData[0] = new byte[receivedPacket.getLength() - 16];
            System.arraycopy(data, 16, packetData[0], 0, packetData[0].length);
            return 0;
        } catch ( IOException e ) {
            if ( !this.permanent ) {
                this.stopOnGet = true;
            }
            return -1;
        }
    }
}
