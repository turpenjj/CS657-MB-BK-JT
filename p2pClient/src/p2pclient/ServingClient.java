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
public class ServingClient extends Util implements Runnable {
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
//        System.out.println("Started a new thread for ServingClient");
//        System.out.println("ChunkManager length: " + chunkManagers.length);
//        for ( int i = 0; i < chunkManagers.length; i++ ) {
//            System.out.println("ChunkManager[" + i + "] " + chunkManagers[i].filename);
//        }

        //Spin up a thread for listening on the socket.
        listener = new MessageReceive(listeningPort, acceptedPackets, true);
        listener.start();
System.out.println("ServingClient listening on port " + listeningPort);
        //Continually poll the listener for requests, then determine if we want
        //to respond and respond if we want
        for ( ;; ) {
            PacketType[] packetType = new PacketType[1];
            int[] sessionID = new int[1];
            sessionID[0] = 0;
            Peer[] peer = new Peer[1];
            byte[][] packetInData = new byte[1][];
            if ( listener.GetMessage(0, acceptedPackets, peer, packetType, sessionID, packetInData)) {
                byte[] packetData = packetInData[0];
                System.out.println("Got a message: " + Util.ConvertToHex(packetData) + " from " + peer[0].clientIp);
                peerManager.UpdatePeer(peer[0]);
                switch (packetType[0]) {
                    case CHUNK_LIST_REQUEST:
                        ChunkListRequest chunkListRequest = new ChunkListRequest();
                        chunkListRequest.ImportMessagePayload(packetData);
                        SendChunkListResponse(peer[0], chunkListRequest, sessionID[0]);
                        break;
                    case CHUNK_REQUEST:
                        if ( peerManager.ShouldTradeWith(peer[0]) ) {
                            ChunkRequest chunkRequest = new ChunkRequest();
                            chunkRequest.ImportMessagePayload(packetData);
                            SendChunkResponse(peer[0], chunkRequest, sessionID[0]);
                            peerManager.AddCreditForUsToPeer(peer[0]);
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
            System.out.println("Failed to find an appropriate chunk manager");
            return;
        }
        ChunkListResponse chunkListResponse = new ChunkListResponse(chunkManager.filename, chunkManager.AvailableChunks());
        MessageSend sender = new MessageSend();
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
