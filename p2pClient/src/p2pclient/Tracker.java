package p2pclient;

import java.io.*;
import java.net.*;

/**
 * Description:
 *  This is the top-level class for the Tracker component. This component has a
 *  single listening thread and socket.
 *
 *      Client's registration of file(s) they are currently sharing (TRACKER_REGISTRATION)
 *      Client's search queries (TRACKER_QUERY)
 *      Client's registration of a file (torrent) for which they have the complete file (TRACKER_TORRENT_REGISTRATION)
 *      
 *  All registrations have a timeout. If the registration of a torrent times out
 *  and the tracker is still aware of someone sharing at least part of that file,
 *  the torrent timeout is reset. If a client's file list times out, that list is
 *  simply removed (clients should be sending periodic updates to the tracker).
 *
 * @author Jeremy
 */
public class Tracker implements Runnable {
    private Thread runner;
    private MessageReceive messageReceiver; // receives messages on the tracker listening port
    private MessageSend messageSender; // all sends are done on the listening thread in response to queries
    private int listeningPort;
    private TrackerRegistration registeredPeers; // tracks all peers registered with the tracker
    private TrackerTorrentRegistration registeredTorrents; // tracks all torrents registered with the tracker
    // message types the tracker is listening for
    private PacketType[] acceptedPacketTypes = {
        PacketType.TRACKER_QUERY, PacketType.TRACKER_REGISTRATION,
        PacketType.TRACKER_TORRENT_REGISTRATION, PacketType.TRACKER_TORRENT_QUERY};
    public static int TRACKER_LISTENING_PORT = 51966;

    Tracker(int listeningPort) {
        this.listeningPort = listeningPort;
        this.registeredPeers = new TrackerRegistration();
        this.registeredTorrents = new TrackerTorrentRegistration();
        this.messageSender = new MessageSend();
        // we wait to create the messageReceiver until the thread starts
    }

    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        Peer[] peer = new Peer[1];
        PacketType[] packetType = new PacketType[1];
        int[] sessionID = new int[1];
        byte[] messageData = null;

        Util.DebugPrint(DbgSub.TRACKER, "Started a Tracker thread on port " + this.listeningPort);

        this.messageReceiver = new MessageReceive(this.listeningPort, this.acceptedPacketTypes, true);
        this.messageReceiver.start();

        for (;;) {
            if ((messageData = this.messageReceiver.GetMessage(0, this.acceptedPacketTypes, peer, packetType, sessionID)) != null) {
                ProcessQuery(peer[0], packetType[0], sessionID[0], messageData);
            } else {
                try {
                    Thread.sleep(5);
                } catch ( InterruptedException e ) {}
            }

        }
    }

    /**
     * Dispositions a full message once it has been received. Makes use of other
     * classes (TrackerQuery, TrackerQueryResponse, TrackerRegistration,
     * TrackerTorrentRegistration as necessary).
     */
    private void ProcessQuery(Peer peer, PacketType packetType, int sessionID, byte[] messageData) {
        byte[] responseData;
        Peer[] peers;
        Torrent torrent;

        switch (packetType) {
            case TRACKER_QUERY:
                TrackerQuery query = new TrackerQuery();
                if (query.ImportQuery(messageData)) {
                    peers = this.registeredPeers.Search(query.filename);

                    if (peers == null) {
                        peers = new Peer[0];
                    }

                    TrackerQueryResponse response = new TrackerQueryResponse(peers);
                    responseData = response.ExportResponse();
                    // the port it came in on is not the port we respond to, we
                    // respond to the listening port as contained in the query
                    peer.listeningPort = query.listeningPort;

                    Util.DebugPrint(DbgSub.TRACKER, "***********Sending a TRACKER_QUERY_RESPONSE packet to address " + peer.clientIp + ":" + peer.listeningPort + "**********");
                    messageSender.SendCommunication(peer, PacketType.TRACKER_QUERY_RESPONSE, sessionID, responseData);
                }

                break;

            case TRACKER_TORRENT_QUERY:
                TrackerTorrentQuery torrentQuery = new TrackerTorrentQuery();
                if (torrentQuery.ImportQuery(messageData)) {
                    torrent = this.registeredTorrents.Search(torrentQuery.filename);

                    if (torrent == null) {
                        torrent = new Torrent("", 0, new ChunkInfo[0]);
                    }
                    TrackerTorrentResponse torrentResponse = new TrackerTorrentResponse(torrent);
                    responseData = torrentResponse.ExportMessagePayload();

                    // the port it came in on is not the port we respond to, we
                    // respond to the listening port as contained in the query
                    peer.listeningPort = torrentQuery.listeningPort;

                    messageSender.SendCommunication(peer, PacketType.TRACKER_TORRENT_RESPONSE, sessionID, responseData);
                }

                break;

            case TRACKER_REGISTRATION:
                this.registeredPeers.ImportMessage(peer, messageData);

                /**
                 * There is no response to a registration message. When the peers
                 * have a state change (first come up, new files being shared),
                 * they simply send their full list to the tracker. They also
                 * periodically send their full list because the tracker will
                 * age them out otherwise (don't want the tracker giving out
                 * that host if it went down).
                 */

                break;

            case TRACKER_TORRENT_REGISTRATION:
                this.registeredTorrents.ImportMessagePayload(messageData);

                /**
                 * There is no response to a torrent registration message. When
                 * the peers have a state change (first come up, new files being
                 * shared), they search for the torrent and if it is not registered
                 * they register it.
                 *
                 * This is a performance optimization (why send the torrent if
                 * it is already registered).
                 */

                break;

            default:
                Util.DebugPrint(DbgSub.TRACKER, "Received an unsupported packetType " + packetType);

                break;
        }
    }
}
