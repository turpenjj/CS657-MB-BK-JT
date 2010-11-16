package p2pclient;

/**
 * Defines the message format for the Tracker Torrent Registration message type
 * (peers registering a torrent, complete file metadata, with the tracker).
 * Provides methods for peers to create such messages and methods for the tracker
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
 * @note The Tracker Torrent Registration message data format is the same as the
 * Tracker Torrent Response message data format.
 * 
 * Torrents are removed from the list when no one has registered them within a
 * certain timeout window AND no one is sharing any chunks.
 *
 * @author Jeremy
 */
public class TrackerTorrentRegistration {
    // will only be one element on the peer (the single torrent it is registering)
    public Torrent[] registeredTorrents;
    private int MESSAGE_BASE_SIZE = 1 + 4 + 4; // filename null terminator (1), filesize (4), numberOfChunks (4)
    private int CHUNK_HASH_SIZE = 20; // sha1sum size
    private int MESSAGE_CHUNK_DATA_SIZE = 4 + CHUNK_HASH_SIZE; // chunkNumber (4), sha1sum

    /**
     * Peer constructor. Called when creating the registration, followed by a
     * call to ExportMessagePayload.
     *
     * @param torrent Torrent to register
     */
    TrackerTorrentRegistration(Torrent torrent) {
        this.registeredTorrents = new Torrent[1];
        this.registeredTorrents[0] = torrent;
    }

    /**
     * Tracker constructor. Called to instantiate the one torrent registration
     * object for the tracker. Updates occur via calls to ImportMessagePayload.
     */
    TrackerTorrentRegistration() {
        this.registeredTorrents = new Torrent[0];
    }

    /**
     * @param filename Filename being searched for
     *
     * @return Torrent if it has been registered, NULL otherwise
     */
    public Torrent Search(String filename) {
        RefreshList();

        // iterate through Torrent list and find it
        for (Torrent torrent : this.registeredTorrents) {
            if (torrent.filename.equalsIgnoreCase(filename)) {
                return torrent;
            }
        }

        return null;
    }

    /**
     * Tracker function, de-registers the given torrent.
     * Might not be needed if we change GetAllTorrents to purge stale stuff
     *
     * @param torrent Torrent to deregister
     */
    public void DeregisterTorrent(Torrent torrent) {
        int currentIndex = 0;
        Torrent[] tempTorrents;

        for (Torrent loopTorrent : this.registeredTorrents) {
            if (torrent.filename.equalsIgnoreCase(loopTorrent.filename)) {
                tempTorrents = new Torrent[this.registeredTorrents.length - 1];
                System.arraycopy(this.registeredTorrents, 0, tempTorrents, 0, currentIndex);
                System.arraycopy(this.registeredTorrents, currentIndex + 1, tempTorrents, currentIndex, tempTorrents.length - currentIndex);
                this.registeredTorrents = tempTorrents;
                continue;
            }
            currentIndex++;
        }
    }

    /**
     * Exports the payload portion of a TRACKER_TORRENT_REGISTRATION message
     * corresponding to a torrent. Expected to be called by a peer.
     *
     * @return Message data payload
     */
    public byte[] ExportMessagePayload() {
        byte[] blob;
        int currentIndex = 0;
        Torrent torrent = this.registeredTorrents[0];
        int totalSize = this.MESSAGE_BASE_SIZE +
                torrent.filename.length() +
                torrent.numChunks * this.MESSAGE_CHUNK_DATA_SIZE;

        blob = new byte[totalSize];

        currentIndex = Util.InsertNullTerminatedString(blob, currentIndex, torrent.filename);
        currentIndex = Util.IntToByteArray(blob, currentIndex, torrent.filesize);
        currentIndex = Util.IntToByteArray(blob, currentIndex, torrent.numChunks);

        for (ChunkInfo chunkInfo : torrent.chunks) {
            currentIndex = Util.IntToByteArray(blob, currentIndex, chunkInfo.chunkNumber);
            System.arraycopy(chunkInfo.hash, 0, blob, currentIndex, this.CHUNK_HASH_SIZE);
            currentIndex += this.CHUNK_HASH_SIZE;
        }

        return blob;
    }

    /**
     * Imports the payload portion of a TRACKER_TORRENT_REGISTRATION message
     * corresponding to a torrent. Expected to be called by the tracker.
     *
     * @param data Message data payload
     */
    public void ImportMessagePayload(byte[] blob) {
        int currentIndex = 0;
        int[] nextIndex = new int[1];
        Torrent torrent;
        String filename;
        int filesize;
        int numberOfChunks;
        int chunkNumber;
        byte[] chunkSha1sum = new byte[this.CHUNK_HASH_SIZE];
        int i;
        ChunkInfo[] chunkInfoList = null;
        ChunkInfo chunkInfo;

        if (blob.length < this.MESSAGE_BASE_SIZE) {
            Util.DebugPrint(DbgSub.TRACKER_TORRENT_REGISTRATION, "Message not large enough");

            return;
        }

        filename = Util.ExtractNullTerminatedString(blob, currentIndex, nextIndex);
        currentIndex = nextIndex[0];
        filesize = Util.ByteArrayToInt(blob, currentIndex);
        currentIndex += 4;
        numberOfChunks = Util.ByteArrayToInt(blob, currentIndex);
        currentIndex += 4;

        if (numberOfChunks * this.MESSAGE_CHUNK_DATA_SIZE != blob.length - currentIndex) {
            Util.DebugPrint(DbgSub.TRACKER_TORRENT_REGISTRATION, "Mismatch between number of chunks and message data remaining");

            return;
        }

        for (i = 0; i < numberOfChunks; i++) {
            chunkNumber = Util.ByteArrayToInt(blob, currentIndex);
            currentIndex += 4;
            System.arraycopy(blob, currentIndex, chunkSha1sum, 0, this.CHUNK_HASH_SIZE);
            currentIndex += this.CHUNK_HASH_SIZE;

            chunkInfo = new ChunkInfo(chunkNumber, chunkSha1sum, 2);
            chunkInfoList = Torrent.AddToChunkInfoList(chunkInfoList, chunkInfo);
        }

        torrent = new Torrent(filename, filesize, chunkInfoList);
        this.registeredTorrents = AddToTorrentList(this.registeredTorrents, torrent);
    }

    private Torrent[] AddToTorrentList(Torrent[] list, Torrent torrent) {
        Torrent[] temp;

        for (Torrent tempTorrent : list) {
            // don't add duplicates
            if (tempTorrent.filename.equalsIgnoreCase(torrent.filename)) {
                return list;
            }
        }

        if (list == null || list.length == 0 || list[0] == null) {
            temp = new Torrent[1];
            temp[0] = torrent;

            return temp;
        }

        temp = new Torrent[list.length + 1];

        temp[0] = torrent;
        System.arraycopy(list, 0, temp, 1, list.length);

        return temp;
    }

    /**
     * Gets a list of all registered torrents.
     *
     * Consider filtering out any stale (nobody is advertising)
     * @return
     */
    public Torrent[] GetAllTorrents() {
        return this.registeredTorrents;
    }

    private void RefreshList() {
        // TODO: fill me in
        // iterate through Torrent list and remove any entries that have timed out
    }
}
