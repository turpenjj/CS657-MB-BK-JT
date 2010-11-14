/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.net.*;

/**
 * Description:
 *  This class is the client program running on a p2p Host that sends out
 *  requests for chunk lists and chunks to other peers and receives incoming
 *  chunks
 *
 * @author Matt
 */
public class RequestingClient extends Util implements Runnable {
    Thread runner;
    Peer servingHost;
    String requestedFilename;
    long timestamp;

    RequestingClient(Peer host, String filename) {
        servingHost = host;
        requestedFilename = filename;
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

        GetChunkListFromPeer();

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
     */
    public void RequestChunk(int chunkNumber) {

    }

    /*
     * Description:
     *   Check status of the last ChunkRequest
     *   Returns a status of either 0 (timed out), 1 (chunk received), or
     *   2 (still waiting)
     *   If status is returned as 1, chunkData will contain the chunk data
     */
    public int ChunkStatus(int chunkNumber, byte[] chunkData) {
        return 0;
    }

    /*
     * Description:
     *   Requests the chunk list for the given filename.
     */
    public void RequestChunkList(String filename) {

    }

    /*
     * Description:
     *   Checks status of the last ChunkListRequest
     *   Returns a status of either 0 (timed out), 1 (chunk received), or
     *   2 (still waiting)
     *   If status is returned as 1, chunkList will contain the chunk list
     */
    public int ChunkListStatus(String filename, int[] chunkList) {
        return 0;
    }
    /********************************************/


    public int[] GetChunkListFromPeer() {
        int receivingPort = 54321;
        //TODO: Send UDP request to peer for list of chunks
        if ( !SendListRequest(receivingPort) ) {
            return null;
        }
        return null;

//        peerChunkList = ReceiveListResponse(servingHost, receivingPort);

        //TODO: Wait for chunk list response (if timeout, resend)

        //TODO: Parse chunk list response and list available chunks we don't have

//        return peerChunkList;
    }

        //TODO: (optional) Determine which chunks are "rare"

    public FileChunk GetChunkFromPeer(int chunkNumber) {
        return null;
//        FileChunk receivedChunk = new FileChunk(requestedFilename, chunkNumber);

        //TODO: Send UDP request for specific chunk

        //TODO: Wait for chunk response (if timeout, resend)

        //TODO: Verify chunk hash against value in torrent file

//        return receivedChunk;
    }

    private boolean SendListRequest(int receivingPort) {
        ChunkListRequest chunkListRequest = new ChunkListRequest(servingHost, receivingPort, requestedFilename);
        int requestLength = chunkListRequest.GetSize();
        PacketHeader packetHeader = new PacketHeader(0, PacketType.CHUNK_LIST_REQUEST.ordinal(), requestLength, 0);
        byte[] packetHeaderInBytes = packetHeader.GetBytes();
        int packetLength = requestLength + packetHeaderInBytes.length;
        byte[] sendBuffer = new byte[packetLength];

        //Copy the packet header into our sending buffer, then the chunk list request
        System.arraycopy(packetHeaderInBytes, 0, sendBuffer, 0, packetHeaderInBytes.length);
        System.arraycopy(chunkListRequest.ExportMessagePayload(), 0, sendBuffer, packetHeaderInBytes.length, requestLength);

        return SendPacket(servingHost, sendBuffer);
    }

    private int[] ReceiveListResponse(Peer peer, int listeningPort) {
        PacketHeader packetHeader;
        boolean morePackets = false;
        byte[] rawChunkList = null;
        int[] chunkList;
        int chunkListSize = ReceiveCommunication(peer, listeningPort, rawChunkList);
        chunkList = ByteArrayToIntArray(rawChunkList);

        int bytesRead;
        int sessionID = -1;
        int totalBytesRead = 0;
        
        do {
            byte[] rawReceivedData = null;
            int[] tempIntList;
            bytesRead = ReceivePacket(peer, listeningPort, rawReceivedData);
            if ( bytesRead < 16 ) {
                //Failed to read enough data for Packet Header
                return null;
            }
            tempIntList = ByteArrayToIntArray(rawReceivedData);
            packetHeader = new PacketHeader(tempIntList[0], tempIntList[1], tempIntList[2], tempIntList[3]);
            if ( sessionID == -1 ) {
                sessionID = packetHeader.sessionID;
            } else if ( sessionID != packetHeader.sessionID ) {
                System.out.println("Received packet for the wrong session, dropping");
                return null;
            }
            totalBytesRead += bytesRead - 16;
            if ( totalBytesRead < packetHeader.totalSize ) {
                morePackets = true;
            }
            rawChunkList = new byte[bytesRead-16];
            System.arraycopy(rawReceivedData, 16, rawChunkList, 0, bytesRead - 16);            
        } while ( morePackets );
        
        //Chunk list starts after packet header
        chunkList = ByteArrayToIntArray(rawChunkList);


        return chunkList;
    }

}
