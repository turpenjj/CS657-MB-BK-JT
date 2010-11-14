/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;

/**
 * Description:
 *  This class is the client program running on a p2p Host that receives,
 *  processes, and responds to requests for chunk lists and chunks.
 *
 * @author Matt
 */
public class ServingClient extends Util implements Runnable {
    Thread runner;
    int listeningPort;
    private MessageReceive listener;
    private PacketType[] acceptedPackets;
    private ChunkManager[] chunkManagers;
    private PeerManager peerManager;

    ServingClient(int port, ChunkManager[] chunkManagers, PeerManager peerManager) {
        listeningPort = port;
        this.chunkManagers = chunkManagers;
        this.peerManager = peerManager;
    }

    //Creates and starts a new ServingClient thread;
    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void UpdateChunkManagers(ChunkManager[] chunkManagers) {
        this.chunkManagers = chunkManagers;
    }

    public void run() {
        System.out.println("Started a new thread for ServingClient");
        System.out.println("ChunkManager length: " + chunkManagers.length);
        for ( int i = 0; i < chunkManagers.length; i++ ) {
            System.out.println("ChunkManager[" + i + "] " + chunkManagers[i].filename);
        }

        //Spin up a thread for listening on the socket.
        CreateListener();

        //Continually poll the listener for requests, then determine if we want
        //to respond and respond if we want
        for ( ;; ) {
            PacketType packetType = null;
            int[] sessionID = new int[1];
            sessionID[0] = 0;
            Peer peer = null;
            byte[] packetData = null;
            if ( listener.GetMessage(0, acceptedPackets, peer, packetType, sessionID, packetData)) {
                switch (packetType) {
                    case CHUNK_LIST_REQUEST:
                        ChunkListRequest chunkListRequest = new ChunkListRequest();
                        chunkListRequest.ImportMessagePayload(packetData);
                        SendChunkListResponse(peer, chunkListRequest, sessionID[0]);
                        break;
                    case CHUNK_REQUEST:
                        if ( peerManager.ShouldTradeWith(peer) ) {
                            ChunkRequest chunkRequest = new ChunkRequest();
                            chunkRequest.ImportMessagePayload(packetData);
                            SendChunkResponse(peer, chunkRequest, sessionID[0]);
                            peerManager.AddCreditForUsToPeer(peer);
                        }
                        break;
                }
            }
        }
    }

    private void SendChunkListResponse(Peer peer, ChunkListRequest chunkListRequest, int sessionID) {
        ChunkManager chunkManager = null;
        int chunkManagerIndex = 0;
        while ( chunkManager == null && chunkManagers[chunkManagerIndex] != null ) {
            chunkManager = chunkManagers[chunkManagerIndex++].FindChunkManager(chunkListRequest.filename);
        }
        ChunkListResponse chunkListResponse = new ChunkListResponse(chunkManager.filename, chunkManager.AvailableChunks());
        SendCommunication(peer, PacketType.CHUNK_LIST_RESPONSE, sessionID, chunkListResponse.ExportMessagePayload());
    }

    private void SendChunkResponse(Peer peer, ChunkRequest chunkRequest, int sessionID) {
        ChunkManager chunkManager = null;
        int chunkManagerIndex = 0;
        while( chunkManager == null && chunkManagers[chunkManagerIndex] != null ) {
            chunkManager = chunkManagers[chunkManagerIndex++].FindChunkManager(chunkRequest.filename);
        }
        ChunkResponse chunkResponse = new ChunkResponse(chunkManager.filename, chunkRequest.chunkNumber, chunkManager.GetChunkData(chunkRequest.chunkNumber));
        SendCommunication(peer, PacketType.CHUNK_RESPONSE, sessionID, chunkResponse.ExportMessagePayload());
    }


    /*
     * Description:
     *   Creates and binds a socket for receiving new requests
     */
    private void CreateListener() {
        acceptedPackets = new PacketType[2];
        acceptedPackets[0] = PacketType.CHUNK_LIST_REQUEST;
        acceptedPackets[1] = PacketType.CHUNK_REQUEST;
        listener = new MessageReceive(listeningPort, acceptedPackets);
        listener.start();
    }

    /*
     * Description:
     *   Responds to a Chunk List Request
     */
    private void HandleChunkListRequest() {
        
    }

    /*
     * Description:
     *   Responds to a Chunk Request
     */
    private void HandleChunkRequest() {
        
    }

    /*
     * Description:
     *   Tells this ServingClient thread to stop.
     */
    public void Stop() {

    }
}
