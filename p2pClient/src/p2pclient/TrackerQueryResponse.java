package p2pclient;

import java.net.*;

/**
 * This class defines the structures for the tracker query response message and
 * provides methods for creating such messages. The structure of the message
 * payload on the wire is:
 *
 * uint32_t numberOfPeers;
 * Peers[] {
 *      uint32_t ip;
 *      uint32_t listeningPort;
 * }
 * 
 * @author Jeremy
 */
public class TrackerQueryResponse {
    public Peer[] peerList;
    private int QUERY_RESPONSE_DATA_BASE_SIZE = 4; // number of peers (4)
    private int PEER_ENTRY_SIZE = 8; // ip (4) + listening port (4)

    /**
     * Tracker constructor. Called when creating the response, followed by a call to
     * ExportMessagePayload.
     *
     * @param peerList List of peers that are sharing parts of the a given file
     */
    TrackerQueryResponse(Peer[] peerList) {
        this.peerList = new Peer[peerList.length];
        System.arraycopy(peerList, 0, this.peerList, 0, peerList.length);
    }

    /**
     * Peer constructor. Called when receiving the query, followed by a call
     * to ImportMessagePayload. Necessary data then available via public data
     * members.
     */
    TrackerQueryResponse() {
        // intentionally empty
    }

    /**
     * Would expect the tracker to call this.
     *
     * Returns the query response structure as it should exist on the wire,
     * which is simply a list of Peers
     */
    public byte[] ExportResponse() {
        byte[] messageData = new byte[this.QUERY_RESPONSE_DATA_BASE_SIZE + 
                (this.peerList.length * this.PEER_ENTRY_SIZE)];
        int currentIndex = 0;

        currentIndex = Util.IntToByteArray(messageData, currentIndex, this.peerList.length);

        for (Peer peer : this.peerList) {
            System.arraycopy(peer.clientIp.getAddress(), 0, messageData, currentIndex, 4);
            currentIndex += 4;
            currentIndex = Util.IntToByteArray(messageData, currentIndex, peer.listeningPort);

            if (currentIndex >= messageData.length) {
                break;
            }
        }

        return messageData;
    }

    /**
     * Would expect a peer to call this. Initialize an instance from a data byte stream.
     *
     * @param data Byte stream from the wire
     * @return
     */
    public boolean ImportResponse(byte[] data) {
        int numberOfPeers = 0;
        int currentIndex = 0;
        int dataLeft = data.length;
        byte[] IP = new byte[4];
        int port;
        Peer peer;
        int i = 0;

        if (data.length < this.QUERY_RESPONSE_DATA_BASE_SIZE) {
            return false;
        }

        numberOfPeers = Util.ByteArrayToInt(data, currentIndex);
        currentIndex += 4;
        dataLeft -= 4;

        if (dataLeft < numberOfPeers * this.PEER_ENTRY_SIZE) {
            return false;
        }

        this.peerList = new Peer[numberOfPeers];
        while ((dataLeft >= this.PEER_ENTRY_SIZE) && (i <= numberOfPeers)) {
            System.arraycopy(data, currentIndex, IP, 0, 4);
            currentIndex += 4;
            dataLeft -= 4;

            port = Util.ByteArrayToInt(data, currentIndex);
            currentIndex += 4;
            dataLeft -= 4;

            try {
                peer = new Peer(InetAddress.getByAddress(IP), port);
                this.peerList[i] = peer;
                i++;
            } catch ( UnknownHostException e ) {
                //TODO: shrink peerList by one...
            }
        }

        return true;
    }
}
