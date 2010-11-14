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
    RequestingClientListener[] listeners;

    RequestingClient(Peer host, String requestedFilename) {
        servingHost = host;
        filename = requestedFilename;
        listeners = new RequestingClientListener[MAX_LISTENERS];
        for ( int i = 0; i < MAX_LISTENERS; i++ ) {
            listeners[i] = new RequestingClientListener();
        }
    }

    /*
     * Description:
     *   Finds the first available listener to use.  If no listeners are
     *   available, then -1 is returned.
     */
    private int FindAvailableListener() {
        int listenerIndex = 0;
        while (listenerIndex < MAX_LISTENERS) {
            if ( listeners[listenerIndex].available == true ) {
                return listenerIndex;
            }
            listenerIndex++;
        }
        return -1;
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

//        GetChunkListFromPeer();

        //TODO: Get Chunks

        //TODO: Update list of chunks we have
    }

    /*
     * Description:
     *   Stops this requesting client thread
     */
    public void Stop() {

    }

    /*
     * Description:
     *   Requests the given chunk number.
     * Returns the index of the listener it is using
     */
    public int RequestChunk(int chunkNumber) {
        System.out.println("here");
        ChunkRequest test = new ChunkRequest();
        byte[] temp = new byte[14];
        System.arraycopy("hello".getBytes(), 0, temp, 8, "hello".length());
        test.ImportMessagePayload(temp);
        int listenerIndex = FindAvailableListener();
        if ( listenerIndex == -1 ) {
            return -1;
        }
        listeners[listenerIndex].start();
        ChunkRequest chunkRequest = new ChunkRequest(filename, chunkNumber, listeners[listenerIndex].listeningPort);
        SendPacket(servingHost, chunkRequest.ExportMessagePayload());
        return listenerIndex;
    }

    /*
     * Description:
     *   Check status of the last ChunkRequest
     *   Returns a status of either 0 (timed out), 1 (chunk received), or
     *   2 (still waiting)
     *   If status is returned as 1, chunkData will contain the chunk data
     */
    public int ChunkStatus(int listenerIndex, int chunkNumber, byte[] chunkData) {
        ChunkResponse chunkResponse = new ChunkResponse();
        byte[] rawReceivedData = null;
        int requestStatus = listeners[listenerIndex].CheckResponse(rawReceivedData);
        if ( rawReceivedData != null ) {
            chunkResponse.ImportMessagePayload(rawReceivedData);
            chunkNumber = chunkResponse.chunkNumber;
            chunkData = chunkResponse.chunkData;
        }
        return requestStatus;
    }

    /*
     * Description:
     *   Requests the chunk list for the given filename.
     *
     * Returns:
     *   The index of the listener this request is using
     */
    public int RequestChunkList() {
        int listenerIndex = FindAvailableListener();
        if ( listenerIndex == -1 ) {
            return -1;
        }
        listeners[listenerIndex].start();

        ChunkListRequest chunkListRequest = new ChunkListRequest(filename, listeners[listenerIndex].listeningPort);
        SendPacket(servingHost, chunkListRequest.ExportMessagePayload());
        return listenerIndex;
    }

    /*
     * Description:
     *   Checks status of the last ChunkListRequest
     *   Returns a status of either 0 (timed out), 1 (chunk received), or
     *   2 (still waiting)
     *   If status is returned as 1, chunkList will contain the chunk list
     */
    public int ChunkListStatus(int listenerIndex, String filename, int[] chunkList) {
        ChunkListResponse chunkListResponse = new ChunkListResponse();
        byte[] rawReceivedData = null;
        int requestStatus = listeners[listenerIndex].CheckResponse(rawReceivedData);
        if ( rawReceivedData != null ) {
            chunkListResponse.ImportMessagePayload(rawReceivedData);
            chunkList = chunkListResponse.chunkList;
        }
        return requestStatus;
    }
}

class RequestingClientListener implements Runnable {
    private int BASE_SOCKET_PORT = 2000;
    private int MAX_PORT_NUMBER = 65000;
    private int TIMEOUT_VALUE = 10000;
    private DatagramSocket listeningSocket;
    private volatile Thread runner;
    private long timeout;
    public volatile int listeningPort;
    public volatile boolean available;
    byte[] receivedData;

    RequestingClientListener() {
        System.out.println("Creating a listener");
        available = true;
    }

    public void start() {
        if ( runner == null ) {
            listeningPort = BASE_SOCKET_PORT;
            available = false;
            while ( listeningPort < MAX_PORT_NUMBER )  {
                try {
                    listeningSocket = new DatagramSocket(listeningPort);
                    System.out.println("Opened a socket on port " + listeningPort);
                    break;
                } catch ( IOException e ) {
                    listeningPort += 1;
                } finally {

                }
            }
            receivedData = null;
            timeout = (Calendar.getInstance()).getTimeInMillis() + TIMEOUT_VALUE;
            System.out.println("Timeout at " + timeout);
            runner = new Thread(this);
            runner.start();
        }
    }

    //This is what gets run when the thread is started
    public void run() {
        System.out.println("Started a listener");
        Thread thisThread = Thread.currentThread();
        while ( runner == thisThread ) {
            try {
                byte[] testData = new byte[1024];
                if ( timeout < (Calendar.getInstance()).getTimeInMillis() ) {
                    DatagramPacket test = new DatagramPacket(testData, testData.length);
                    listeningSocket.receive(test);
                    //Receive message here
                }
                thisThread.sleep(1);
            } catch ( InterruptedException e ) {
            } catch ( IOException e ) {
            }
        }
    }

    public void stop() {
        System.out.println("Stopping thread");
        listeningSocket.close();
        runner = null;
        available = true;
    }

    public int CheckResponse(byte[] data) {
        if (timeout < (Calendar.getInstance()).getTimeInMillis()) {
            System.out.println("Our receiving thread timed out");
            return 0;
        }
        System.out.println("Checked response");
        return 2;
    }
}





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

