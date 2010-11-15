/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.net.*;
import java.util.Calendar;

/**
 * Description:
 *  This class is the client program running on a p2p Host that sends out
 *  requests for chunk lists and chunks to other peers and receives incoming
 *  chunks
 *
 * @author Matt
 */
public class RequestingClient extends Util implements Runnable {
    int MAX_LISTENERS = 10;
    Thread runner;
    Peer servingHost;
    String filename;
    long timestamp;
    Listener[] listenerList;
    private int nextSessionID = 1;
    private int DEFAULT_LISTEN_TIMEOUT = 15000;
    private int LIST_REQUEST_FREQUENCY = 20000;
    private ChunkManager chunkManager;
    private PeerManager peerManager;

    RequestingClient(Peer host, String requestedFilename, ChunkManager chunkManager, PeerManager peerManager) {
        this.servingHost = host;
        this.filename = requestedFilename;
        this.chunkManager = chunkManager;
        this.peerManager = peerManager;
    }

    //Creates and starts a new RequestingClient thread
    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    //This is what gets run when the thread is started
    public void run() {
        System.out.println("Started a thread for RequestingClient");
        long timeForListRequest = GetCurrentTime() + LIST_REQUEST_FREQUENCY;

        RequestChunkList();
        for ( ;; ) {
            if ( timeForListRequest < GetCurrentTime() ) {
                timeForListRequest = GetCurrentTime() + LIST_REQUEST_FREQUENCY;
                RequestChunkList();
            }
            //TODO: Determine which chunk(s) to spin up requests for

                //Loop through listeners and check response status for each listener
                for ( int i = 0; listenerList != null && i < listenerList.length; i++ ) {
                    PacketType[] packetType = new PacketType[1];
                    byte[][] data = new byte[1][];
                    int status = RequestStatus(listenerList[i], packetType, data);
                    switch (status) {
                        case 0: //still waiting
                            break;
                        case 1: //request complete
                            System.out.println("Completed a Request");
                            switch (packetType[0]) {
                                case CHUNK_LIST_RESPONSE:
                                    ProcessChunkListResponse(data[0]);
                                    break;
                                case CHUNK_RESPONSE:
                                    ProcessChunkResponse(data[0]);
                                    break;
                            }
                            RemoveListenerFromList(listenerList[i]);
                            break;
                        case 2: //timeout
                            if ( packetType[0] == PacketType.CHUNK_RESPONSE ) {
                                chunkManager.chunkList[listenerList[i].chunkNumber].chunkInfo.status = 0;
                            }
                            RemoveListenerFromList(listenerList[i]);
                            break;
                    }
                }
        //TODO: add requests for missing chunks
        //NOTE: Each listener will process and store the results when they are received            
        }
    }

    /*
     * Description:
     *   Stops this requesting client thread
     */
    public void Stop() {

    }

    private int FindListenerIndex(Listener listener) {
        int index = 0;
        while ( index < listenerList.length ) {
            if ( listenerList[index].equals(listener) ) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private synchronized void AddListenerToList(Listener newListener) {
        if ( listenerList == null ) {
            listenerList = new Listener[1];
        } else {
            Listener[] tempListenerList = new Listener[listenerList.length + 1];
            System.arraycopy(listenerList, 0, tempListenerList, 0, listenerList.length);
            listenerList = tempListenerList;
        }
        listenerList[listenerList.length - 1] = newListener;
    }

    private synchronized void RemoveListenerFromList(Listener listenerToRemove) {
        listenerToRemove.listener.Stop();
        if ( listenerList.length == 1 ) {
            listenerList = null;
        } else {
            Listener[] tempListenerList = new Listener[listenerList.length - 1];
            int listenerIndex = FindListenerIndex(listenerToRemove);
            System.arraycopy(listenerList, 0, tempListenerList, 0, listenerIndex);
            System.arraycopy(listenerList, listenerIndex + 1, tempListenerList, listenerIndex, tempListenerList.length - listenerIndex);
            listenerList = tempListenerList;
        }
    }

    private synchronized Listener FindListener(int sessionID) {
        for ( int i = 0; listenerList != null && i < listenerList.length; i++ ) {
            if ( listenerList[i].sessionID == sessionID ) {
                return listenerList[i];
            }
        }
        return null;
    }

    /*
     * Description:
     *   Requests the chunk list for the given filename.
     *
     */
    private synchronized void RequestChunkList() {
        Listener newListener = new Listener(PacketType.CHUNK_LIST_RESPONSE, nextSessionID++, -1, DEFAULT_LISTEN_TIMEOUT);
        AddListenerToList(newListener);
System.out.println("Sending out a new chunk list request " + (nextSessionID - 1));
        ChunkListRequest chunkListRequest = new ChunkListRequest(filename, newListener.listener.listeningPort);
        MessageSend sender = new MessageSend();
        sender.SendCommunication(servingHost, PacketType.CHUNK_LIST_REQUEST, newListener.sessionID, chunkListRequest.ExportMessagePayload());
    }

    /*
     * Description:
     *   Requests the given chunk number.
     */
    private synchronized void RequestChunk(int chunkNumber) {
        Listener newListener = new Listener(PacketType.CHUNK_RESPONSE, nextSessionID++, chunkNumber, DEFAULT_LISTEN_TIMEOUT);
        AddListenerToList(newListener);

        ChunkRequest chunkRequest = new ChunkRequest(filename, chunkNumber, newListener.listener.listeningPort);
        chunkManager.chunkList[chunkNumber].chunkInfo.status = 1; //downloading
        MessageSend sender = new MessageSend();
        sender.SendCommunication(servingHost, PacketType.CHUNK_REQUEST, newListener.sessionID, chunkRequest.ExportMessagePayload());
    }

    /*
     * Description:
     *   Check status of an outstanding request
     *   Returns a status of either 0 (still waiting), 1 (chunk received), or
     *   2 (timed out)
     *   If status is returned as 1, chunkData will contain the chunk data
     */
    private synchronized int RequestStatus(Listener listener, PacketType[] packetType, byte[][] responseData) {
        int status = listener.GetMessage(packetType, responseData);

        return status;
    }

    private synchronized void ProcessChunkListResponse(byte[] rawData) {
        if ( rawData != null ) {
            ChunkListResponse chunkListResponse = new ChunkListResponse();
            chunkListResponse.ImportMessagePayload(rawData);

            System.out.println("Received a chunk list for " + chunkListResponse.filename);
            //TODO: Update peer in Peer Manager
            peerManager.UpdatePeer(servingHost);
            servingHost.UpdateChunkList(filename, chunkListResponse.chunkList);
        }
    }

    private synchronized void ProcessChunkResponse(byte[] rawData) {
        if ( rawData != null ) {
            int chunkNumber;
            byte[] chunkData;
            ChunkResponse chunkResponse = new ChunkResponse();
            chunkResponse.ImportMessagePayload(rawData);
            chunkNumber = chunkResponse.chunkNumber;
            chunkData = chunkResponse.chunkData;
            
            chunkManager.UpdateChunk(chunkNumber, chunkData, servingHost);       
        }        
    }

}

class Listener {
    MessageReceive listener;
    PacketType[] requestType;
    int sessionID;
    int chunkNumber;
    private long timeout;
    private int status; //0 = listening, 1 = messageAvailable, 2 = timedOut

    Listener(PacketType requestType, int sessionID, int chunkNumber, int timeoutValue) {
        PacketType[] packetType = {requestType};
        this.listener = new MessageReceive(packetType);
        this.requestType = packetType;
        this.sessionID = sessionID;
        this.chunkNumber = chunkNumber;
        this.timeout = Util.GetCurrentTime() + timeoutValue;
        this.status = 0;
        this.listener.start();
        System.out.println("SessionID: " + sessionID);
    }

    public int GetMessage(PacketType[] receivedPacketType, byte[][] packetData) {
        if ( (packetData[0] = listener.GetMessage(sessionID, requestType, null, receivedPacketType, null)) != null ) {
            System.out.println("Received a message!");
            listener.Stop();
            status = 1;
        } else if ( Util.GetCurrentTime() > timeout ) {
            System.out.println("requesting Listener timed out");
            //TODO: Implement stop() method for MessageReceive
            listener.Stop();
            status = 2;
        }
        return status;
    }

}

//class RequestingClientListener implements Runnable {
//    private int BASE_SOCKET_PORT = 2000;
//    private int MAX_PORT_NUMBER = 65000;
//    private int TIMEOUT_VALUE = 10000;
//    private DatagramSocket listeningSocket;
//    private volatile Thread runner;
//    private long timeout;
//    public volatile int listeningPort;
//    public volatile boolean available;
//    byte[] receivedData;
//
//    RequestingClientListener() {
//        System.out.println("Creating a listener");
//        available = true;
//    }
//
//    public void start() {
//        if ( runner == null ) {
//            listeningPort = BASE_SOCKET_PORT;
//            available = false;
//            while ( listeningPort < MAX_PORT_NUMBER )  {
//                try {
//                    listeningSocket = new DatagramSocket(listeningPort);
//                    System.out.println("Opened a socket on port " + listeningPort);
//                    break;
//                } catch ( IOException e ) {
//                    listeningPort += 1;
//                } finally {
//
//                }
//            }
//            receivedData = null;
//            timeout = (Calendar.getInstance()).getTimeInMillis() + TIMEOUT_VALUE;
//            System.out.println("Timeout at " + timeout);
//            runner = new Thread(this);
//            runner.start();
//        }
//    }
//
//    //This is what gets run when the thread is started
//    public void run() {
//        System.out.println("Started a listener");
//        Thread thisThread = Thread.currentThread();
//        while ( runner == thisThread ) {
//            try {
//                byte[] testData = new byte[1024];
//                if ( timeout < (Calendar.getInstance()).getTimeInMillis() ) {
//                    DatagramPacket test = new DatagramPacket(testData, testData.length);
//                    listeningSocket.receive(test);
//                    //Receive message here
//                }
//                thisThread.sleep(1);
//            } catch ( InterruptedException e ) {
//            } catch ( IOException e ) {
//            }
//        }
//    }
//
//    public void stop() {
//        System.out.println("Stopping thread");
//        listeningSocket.close();
//        runner = null;
//        available = true;
//    }
//
//    public int CheckResponse(byte[] data) {
//        if (timeout < (Calendar.getInstance()).getTimeInMillis()) {
//            System.out.println("Our receiving thread timed out");
//            return 0;
//        }
//        System.out.println("Checked response");
//        return 2;
//    }
//}
//
//    public int[] GetChunkListFromPeer() {
//        int receivingPort = 54321;
//        //TODO: Send UDP request to peer for list of chunks
//        if ( !SendListRequest(receivingPort) ) {
//            return null;
//        }
//        return null;
//
//        peerChunkList = ReceiveListResponse(servingHost, receivingPort);
//
//        //TODO: Wait for chunk list response (if timeout, resend)
//
//        //TODO: Parse chunk list response and list available chunks we don't have
//
//        return peerChunkList;
//    }
//
//        //TODO: (optional) Determine which chunks are "rare"
//
//    public FileChunk GetChunkFromPeer(int chunkNumber) {
//        return null;
//        FileChunk receivedChunk = new FileChunk(requestedFilename, chunkNumber);
//
//        //TODO: Send UDP request for specific chunk
//
//        //TODO: Wait for chunk response (if timeout, resend)
//
//        //TODO: Verify chunk hash against value in torrent file
//
//        return receivedChunk;
//    }
//
//    private boolean SendListRequest(int receivingPort) {
//        ChunkListRequest chunkListRequest = new ChunkListRequest(receivingPort, filename);
//        int requestLength = chunkListRequest.GetSize();
//        PacketHeader packetHeader = new PacketHeader(0, PacketType.CHUNK_LIST_REQUEST.ordinal(), requestLength, 0);
//        byte[] packetHeaderInBytes = packetHeader.GetBytes();
//        int packetLength = requestLength + packetHeaderInBytes.length;
//        byte[] sendBuffer = new byte[packetLength];
//
//        //Copy the packet header into our sending buffer, then the chunk list request
//        System.arraycopy(packetHeaderInBytes, 0, sendBuffer, 0, packetHeaderInBytes.length);
//        System.arraycopy(chunkListRequest.ExportMessagePayload(), 0, sendBuffer, packetHeaderInBytes.length, requestLength);
//
//        return SendPacket(servingHost, sendBuffer);
//    }
//
//    private int[] ReceiveListResponse(Peer peer, int listeningPort) {
//        PacketHeader packetHeader;
//        boolean morePackets = false;
//        byte[] rawChunkList = null;
//        int[] chunkList;
//        int chunkListSize = ReceiveCommunication(peer, listeningPort, rawChunkList);
//        chunkList = ByteArrayToIntArray(rawChunkList);
//
//        int bytesRead;
//        int sessionID = -1;
//        int totalBytesRead = 0;
//
//        do {
//            byte[] rawReceivedData = null;
//            int[] tempIntList;
//            bytesRead = ReceivePacket(peer, listeningPort, rawReceivedData);
//            if ( bytesRead < 16 ) {
//                //Failed to read enough data for Packet Header
//                return null;
//            }
//            tempIntList = ByteArrayToIntArray(rawReceivedData);
//            packetHeader = new PacketHeader(tempIntList[0], tempIntList[1], tempIntList[2], tempIntList[3]);
//            if ( sessionID == -1 ) {
//                sessionID = packetHeader.sessionID;
//            } else if ( sessionID != packetHeader.sessionID ) {
//                System.out.println("Received packet for the wrong session, dropping");
//                return null;
//            }
//            totalBytesRead += bytesRead - 16;
//            if ( totalBytesRead < packetHeader.totalSize ) {
//                morePackets = true;
//            }
//            rawChunkList = new byte[bytesRead-16];
//            System.arraycopy(rawReceivedData, 16, rawChunkList, 0, bytesRead - 16);
//        } while ( morePackets );
//
//        //Chunk list starts after packet header
//        chunkList = ByteArrayToIntArray(rawChunkList);
//
//
//        return chunkList;
//    }
//
//}

