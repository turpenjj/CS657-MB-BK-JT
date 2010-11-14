package p2pclient;

/**
 * This class tracks the details for all torrents (complete files) that have been registered.
 * Torrents are removed from the list when no one has registered them within a certain timeout window AND no one is sharing any chunks.
 * @author Jeremy
 */
public class TrackerTorrentRegistration {
    // list of torrents and their timeout values (might need an extension of Torrent class to get the timeout values)

    /*
     * Takes a torrent data stream and starts tracking it
     *
     * @return DUPLICATE_REGSITRATION or SUCCESS (does it really matter?)
     */
    public int RegisterTorrent(String filename, byte[] data) {
        // if this entry already exists, update the timeout value
        // if not, add it to the list
        return 0;
    }

    /*
     * @return TRUE if torrent is registered, FALSE otherwise
     */
    public boolean DoesTorrentExist(String filename, byte[] sha1sum) {
        RefreshList();

        // iterate through Torrent list and find it

        return false;
    }

    private void RefreshList() {
        // iterate through Torrent list and remove any entries that have timed out
    }
}

/**
 * The torrent class defines all the attributes of a torrent (complete file and its component chunks). Probably need to move to a Torrent.java file so the clients can use it to create the torrent registration message data.
 *
 * @author Jeremy
 */
class Torrent {
    // filename
    String filename;
   // hash of the file as a whole
    // fileSha1sum
   // array of file chunks
    // may be able to use the FileChunk class, if not we need the chunk size, chunk offset, and chunk sum

    public String GetFilename() {
        return this.filename;
    }

    /**
     * Expected to be called by the tracker
     * 
     * Take the properly formatted byte stream for the tracker torrent
     */
    public void ImportTorrentRegistrationData(byte[] data) {

    }

    /**
     * Expected to be called by a client.
     *
     * Take the internal data and convert it to a properly formatted byte stream for the tracker torrent registration message.
     */
    public byte[] ExportTorrentRegistrationData() {
        return null;
    }

    /**
     * Expected to be called by a client.
     *
     * @param path Location to find the file
     * @param filename Filename without a path
     */
    public void CreateTorrentFromFile(String path, String filename) {
        // read the contents of the file into the chunks in a loop, keeping the total hash up to date as we go
        // save off the hash of the file as a whole
    }
}
