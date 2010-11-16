package p2pclient;

/**
 * Defines the message format for the Tracker Torrent Response message type
 * (tracker responding to a peer request with the torrent).
 * Provides methods for the tracker to create such messages and methods for peers
 * to interpret such messages. The structure of the message payload on the wire
 * is:
 *
 * char[] filename; // null-terminated filename for the torrent
 * uint32_t filesize; // total size of the file
 * uint32_t numberOfChunks; // total number of chunks for this torrent
 * ChunkData[] {
 *      uint32_t chunkNumber; // number of this chunk (zero-based)
 *      byte[CHUNK_HASH_SIZE] sha1sum; // sha1sum for this chunk
 * }
 *
 * CHUNK_HASH_SIZE is 20
 *
 * @note The Tracker Torrent Response message data format is the same as the
 * Tracker Torrent Registration message data format.
 *
 * @note When the tracker doesn't find a torrent matching the search criteria,
 * it returns an empty torrent (filename is an empty string, file size is 0,
 * number of chunks is 0);
 * 
 * @author Jeremy
 */
public class TrackerTorrentResponse {
    private TrackerTorrentRegistration trackerTorrentRegistration = null;
    public Torrent torrent = null;

    /**
     * Peer constructor. Called when receiving the response, followed by a call
     * to ImportMessagePayload. Torrent then accessed via public data members.
     */
    TrackerTorrentResponse() {
        // intentionally empty. all the good stuff happens when ImportMessagePayload is called
    }

    /**
     * Tracker constructor. Called when creating the response, followed by a call
     * to ExportMessagePayload.
     * 
     * @param torrent Torrent to include in the response
     */
    TrackerTorrentResponse(Torrent torrent) {
        this.trackerTorrentRegistration = new TrackerTorrentRegistration(torrent);
    }

    /**
     * Exports the payload portion of a TRACKER_TORRENT_QUERY_RESPONSE message for
     * this torrent.
     *
     * Would expect to be called by the tracker.
     *
     * @return Message payload data as it would exist on the wire.
     */
    public byte[] ExportMessagePayload() {
        if (this.trackerTorrentRegistration == null) {
            return null;
        }

        return this.trackerTorrentRegistration.ExportMessagePayload();
    }

    public void ImportMessagePayload(byte[] blob) {
        Torrent[] torrents;

        this.trackerTorrentRegistration = new TrackerTorrentRegistration();

        this.trackerTorrentRegistration.ImportMessagePayload(blob);

        torrents = this.trackerTorrentRegistration.GetAllTorrents();
        if (torrents != null && torrents.length > 0 && torrents[0] != null) {
            this.torrent = torrents[0];
        }
    }
}
