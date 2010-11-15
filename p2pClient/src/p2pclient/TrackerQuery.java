package p2pclient;

/**
 * This class defines the structure of the tracker query message and provides
 * methods for creating such messages. The structure of the message payload
 * on the wire (following the shared packet header) is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t listeningPort; // port to send the response to
 *
 * @note The Tracker Query message data format is the same as the Tracker Torrent
 * Query message data format.
 *
 * @author Jeremy
 */
public class TrackerQuery {
    public String filename; // name being queried for
    public int listeningPort; // port to send response to
    public int QUERY_DATA_BASE_SIZE = 5; // null string terminator (1) + listeningPort (4)

    /**
     * Peer constructor. Called when creating the query, followed by a call to
     * ExportMessagePayload.
     *
     * @param filename
     * @param listeningPort
     */
    TrackerQuery(String filename, int listeningPort) {
        this.filename = filename;
        this.listeningPort = listeningPort;
    }

    /**
     * Tracker constructor. Called when receiving the query, followed by a call
     * to ImportMessagePayload. Necessary data then available via public data
     * members.
     */
    TrackerQuery() {
        this.filename = "";
        this.listeningPort = 0;
    }

    /**
     * Returns the query structure as it should exist on the wire.
     * Would expect the tracker to call this.
     */
    public byte[] ExportQuery() {
        byte[] blob = new byte[this.filename.length() + this.QUERY_DATA_BASE_SIZE];
        int currentIndex = 0;
        int[] nextIndex = {0};

        if ((nextIndex[0] = Util.InsertNullTerminatedString(blob, currentIndex, this.filename)) == currentIndex) {
            return null;
        }
        currentIndex = nextIndex[0];

        currentIndex = Util.IntToByteArray(blob, currentIndex, this.listeningPort);

        return blob;
    }

    /**
     * Initialize an instance from a data byte stream.
     * Would expect a peer to call this.
     * 
     * @param data
     * @return
     */
    public boolean ImportQuery(byte[] data) {
        byte[] filenameArray;
        byte[] listeningPortArray = new byte[4];
        int[] i = {0};

        if (data.length < this.QUERY_DATA_BASE_SIZE) {
            System.out.println("Import data (" + data.length + ") < base size ("
                    + this.QUERY_DATA_BASE_SIZE + ")");
            return false;
        }

        if ((this.filename = Util.ExtractNullTerminatedString(data, 0, i)) == null) {
            return false;
        }

        if ( i[0] + 4 > data.length) {
            System.out.println("Not enough room for listening port in received data");
            return false;
        }

        this.listeningPort = Util.ByteArrayToInt(data, i[0]);
        i[0] += 4;

        return true;
    }
}
