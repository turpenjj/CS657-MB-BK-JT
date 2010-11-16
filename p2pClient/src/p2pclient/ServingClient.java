/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 * Description:
 *  This class is the client program running on a p2p Host that receives,
 *  processes, and responds to requests for chunk lists and chunks.
 *
 * @author Matt
 */
public class ServingClient implements Runnable {
    Thread runner;
    int listeningPort;
    private MessageReceive listener;
    private PacketType[] acceptedPackets = {PacketType.CHUNK_LIST_REQUEST, PacketType.CHUNK_REQUEST};
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
        Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: listening on port " + listeningPort);

        //Spin up a thread for listening on the socket.
        listener = new MessageReceive(listeningPort, acceptedPackets, true);
        listener.start();
        //Continually poll the listener for requests, then determine if we want
        //to respond and respond if we want
        for ( ;; ) {
            PacketType[] packetType = new PacketType[1];
            int[] sessionID = new int[1];
            sessionID[0] = 0;
            Peer[] peer = new Peer[1];
            byte[] messageData = null;
            if ( (messageData = listener.GetMessage(0, acceptedPackets, peer, packetType, sessionID)) != null ) {
                Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: Got a message from " + peer[0].clientIp);
                Peer persistentPeer = peerManager.UpdatePeer(peer[0]);
                switch (packetType[0]) {
                    case CHUNK_LIST_REQUEST:
                        ChunkListRequest chunkListRequest = new ChunkListRequest();
                        chunkListRequest.ImportMessagePayload(messageData);
                        peer[0].listeningPort = chunkListRequest.receivingPort;
                        Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: Chunk List requested for file " + chunkListRequest.filename);
                        SendChunkListResponse(peer[0], chunkListRequest, sessionID[0]);
                        //Optimization: Add the requested file to the list of files the peer has, since it is likely
                        // that peer is downloading the file from other peers as well
                        persistentPeer.AddFileToList(chunkListRequest.filename);
                        break;
                    case CHUNK_REQUEST:
                        if ( peerManager.ShouldTradeWith(persistentPeer) ) {
                            ChunkRequest chunkRequest = new ChunkRequest();
                            chunkRequest.ImportMessagePayload(messageData);
                            Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: Responding to chunk request for file " + chunkRequest.filename + "(" + chunkRequest.chunkNumber + ")");
                            peer[0].listeningPort = chunkRequest.listeningPort;
                            SendChunkResponse(peer[0], chunkRequest, sessionID[0]);
                            persistentPeer.creditForUs++;
                            persistentPeer.lastServiced = Util.GetCurrentTime();
                            persistentPeer.SentChunk(chunkRequest.filename, chunkRequest.chunkNumber);
                        } else {
                            Util.DebugPrint(DbgSub.SERVING_CLIENT, "Ignoring request from peer, not enough credit");
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
        if ( chunkManager == null ) {
            Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: Failed to find an appropriate chunk manager");
            return;
        }
        ChunkListResponse chunkListResponse = new ChunkListResponse(chunkManager.filename, chunkManager.AvailableChunks());
        MessageSend sender = new MessageSend();
        Util.DebugPrint(DbgSub.SERVING_CLIENT, "ServingClient: sending out chunk list response: " + Util.ConvertToHex(chunkListResponse.ExportMessagePayload()));
        sender.SendCommunication(peer, PacketType.CHUNK_LIST_RESPONSE, sessionID, chunkListResponse.ExportMessagePayload());
    }

    private void SendChunkResponse(Peer peer, ChunkRequest chunkRequest, int sessionID) {
        ChunkManager chunkManager = null;
        int chunkManagerIndex = 0;
        while( chunkManager == null && chunkManagers[chunkManagerIndex] != null ) {
            chunkManager = chunkManagers[chunkManagerIndex++].FindChunkManager(chunkRequest.filename);
        }
        ChunkResponse chunkResponse = new ChunkResponse(chunkManager.filename, chunkRequest.chunkNumber, chunkManager.GetChunkData(chunkRequest.chunkNumber));
        MessageSend sender = new MessageSend();
        sender.SendCommunication(peer, PacketType.CHUNK_RESPONSE, sessionID, chunkResponse.ExportMessagePayload());
    }

    /*
     * Description:
     *   Tells this ServingClient thread to stop.
     */
    public void Stop() {

    }
}
