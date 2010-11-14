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
public class Tracker extends Util implements Runnable {
    private Thread runner;
    private MessageReceive messageReceiver;
    private int listeningPort;
    private TrackerRegistration registeredPeers;
    private TrackerTorrentRegistration registeredTorrents;
    private PacketType[] acceptedPacketTypes = {
        PacketType.TRACKER_QUERY, PacketType.TRACKER_REGISTRATION,
        PacketType.TRACKER_TORRENT_REGISTRATION, PacketType.TRACKER_TORRENT_QUERY};

    Tracker(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        Peer peer = null;
        PacketType packetType = null;
        int sessionID = 0;
        byte[] messageData = null;

        System.out.println("Started a Tracker thread on port " + this.listeningPort);

        this.messageReceiver = new MessageReceive(this.listeningPort, this.acceptedPacketTypes);

        for (;;) {
            if (this.messageReceiver.GetMessage(0, this.acceptedPacketTypes, peer, packetType, sessionID, messageData)) {
                ProcessQuery(peer, packetType, sessionID, messageData);
            } else {
                try {
                    Thread.sleep(100);
                } catch ( InterruptedException e ) {}
            }

        }
    }

    /*
     * Dispositions a full message once it has been received. Makes use of other classes (TrackerQuery, TrackerQueryResponse, TrackerRegistration, TrackerTorrentRegistration as necessary).
     */
    private void ProcessQuery(Peer peer, PacketType packetType, int sessionID, byte[] messageData) {
        switch (packetType) {
            case TRACKER_QUERY:
                TrackerQuery trackerQuery = new TrackerQuery();
                trackerQuery.ImportQuery(messageData);

                

                break;

            case TRACKER_REGISTRATION:


                break;

            case TRACKER_TORRENT_REGISTRATION:

                break;

            case TRACKER_TORRENT_QUERY:

                break;

            default:
                System.out.println("Received an unsupported packetType " + packetType);

                break;
        }
    }

}
