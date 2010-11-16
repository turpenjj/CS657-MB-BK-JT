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
public class RequestingClient implements Runnable {
    int MAX_LISTENERS = 10;
    Thread runner;
    Peer servingHost;
    String filename;
    long timestamp;
    Listener[] listenerList;
    private int nextSessionID = 1;
    private int DEFAULT_LISTEN_TIMEOUT = 5000;
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
        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Started a thread for RequestingClient");
        long timeForListRequest = Util.GetCurrentTime();

        for ( ;; ) {
            if ( timeForListRequest < Util.GetCurrentTime() ) {
                timeForListRequest = Util.GetCurrentTime() + LIST_REQUEST_FREQUENCY;
                RequestChunkList();
                Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "chunkStatus: " + chunkManager.chunkList[1].chunkInfo.status);
            }
            //TODO: Determine which chunk(s) to spin up requests for
            ChunkInfo[] missingChunks = chunkManager.NeededChunks();
            for (int i = 0; i < missingChunks.length; i++) {
                if ( servingHost.HasChunk(filename, i) ) {
                    if ( servingHost.ShouldRequest() ) {
                        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Requesting chunk " + i + " from " + servingHost.clientIp);
                        RequestChunk(i);
                    } else {
                        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Choosing not to request a chunk from " + servingHost.clientIp);
                    }
                }
            }
                //Loop through listeners and check response status for each listener
                for ( int i = 0; listenerList != null && i < listenerList.length; i++ ) {
                    PacketType[] packetType = new PacketType[1];
                    byte[][] data = new byte[1][];
                    int status = RequestStatus(listenerList[i], packetType, data);
                    switch (status) {
                        case 0: //still waiting
                            break;
                        case 1: //request complete
                            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Completed a Request");
Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "MissingChunks: " + chunkManager.NeededChunks().length);
Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "Chunk[1] = " + chunkManager.chunkList[1].chunk);
                            switch (packetType[0]) {
                                case CHUNK_LIST_RESPONSE:
                                    ProcessChunkListResponse(data[0]);
                                    break;
                                case CHUNK_RESPONSE:
                                    servingHost.outstandingRequests--;
                                    ProcessChunkResponse(data[0]);
                                    break;
                            }
                            RemoveListenerFromList(listenerList[i]);
                            break;
                        case 2: //timeout
                            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "11111111111111111Request timed out: " + listenerList[i].chunkNumber );
                            if ( listenerList[i].requestType[0] == PacketType.CHUNK_RESPONSE ) {
                                TimeoutChunkRequest(listenerList[i].chunkNumber);
                                Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "AM I A MORON");
//                                chunkManager.UpdateChunkStatus(listenerList[i].chunkNumber, 0);
//                                Thread.yield();
//                                chunkManager.chunkList[listenerList[i].chunkNumber].chunkInfo.status = 0;
                                servingHost.outstandingRequests--;
                            }
                            RemoveListenerFromList(listenerList[i]);
                            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "!!!!!!!!!!!! missing chunks: " + chunkManager.NeededChunks().length);
                            break;
                    }
                    Thread.yield();
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

    private synchronized void TimeoutChunkRequest(int chunkNumber) {
        chunkManager.chunkList[chunkNumber].chunkInfo.status = 0;
    }

    private synchronized int FindListenerIndex(Listener listener) {
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

        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Sending out a new chunk list request for " + filename);
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

        servingHost.outstandingRequests++;
        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Sending out a new chunk request for " + filename + "-" + chunkNumber);
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

            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Received a chunk list for " + chunkListResponse.filename);
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

            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "ReceivingClient: received chunk " + chunkNumber + " for " + filename);
            chunkManager.UpdateChunk(chunkNumber, chunkData, servingHost);
            servingHost.creditForThem++;
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
        this.listener = new MessageReceive(packetType, true);
        this.requestType = packetType;
        this.sessionID = sessionID;
        this.chunkNumber = chunkNumber;
        this.timeout = Util.GetCurrentTime() + timeoutValue;
        this.status = 0;
        this.listener.start();
        Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: SessionID: " + sessionID);
    }

    public int GetMessage(PacketType[] receivedPacketType, byte[][] packetData) {
        if ( (packetData[0] = listener.GetMessage(sessionID, requestType, null, receivedPacketType, null)) != null ) {
            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: Received a message!");
            listener.Stop();
            status = 1;
        } else if ( Util.GetCurrentTime() > timeout ) {
            Util.DebugPrint(DbgSub.REQUESTING_CLIENT, "RequestingClient: requesting Listener timed out");
            //TODO: Implement stop() method for MessageReceive
            listener.Stop();
            status = 2;
        }
        return status;
    }

}

