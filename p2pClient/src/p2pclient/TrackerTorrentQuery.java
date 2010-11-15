package p2pclient;

/**
 * This class defines the structure of the tracker torrent query message and provides
 * methods for creating such messages. The structure of the message payload
 * on the wire (following the shared packet header) is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t listeningPort; // port to send the response to
 *
 * @note The Tracker Torrent Query message data format is the same as the Tracker
 * Query message data format.
 *
 * @author Jeremy
 */
public class TrackerTorrentQuery extends TrackerQuery {
    /**
     * Empty class. The tracker query is exactly the same, including the format
     * of the message on the wire. The only difference is in the packet type
     * field of the shared header, which is taken care of outside this class.
     */
    TrackerTorrentQuery(String filename, int listeningPort) {
        super(filename, listeningPort);
    }

    TrackerTorrentQuery() {
        super();
    }
}
