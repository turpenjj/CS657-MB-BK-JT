package p2pclient;

/**
 * This handles tracking of all registered peers and the list of files they currently have chunks for. It provides public methods for searching the lists and adding new items to the list. It also handles timing out entries from the lists.
 *
 * We don't have a process/thread for handling timeouts. We simply refresh the lists at the end of each external entry point.
 *
 * @author Jeremy
 */
public class TrackerRegistration {
    String[] filenameList;
    // Need to keep track of peers (Peer), files they have, and timeout value (probably an extension of the Peer class)
    short listeningPort;

    /*
     * Allows the tracker to search for a list of Peers that have chunks to the requested file.
     */
    public Peer[] Search(String filename) {
        RefreshLists();

        // iterate through the list and return any matches
        
        return null;
    }

    /**
     * Adds files from the given directory to the list to be sent with a tracker registration message. Expected to be called by a client.
     * @param path Directory to search for files in
     */
    public void AddFilesFromDirectory(String path) {
        // iterate through the directory and add files to this.filenameList
    }

    /*
     * Processes the Tracker Registration message is it appears on the wire. Expected to be called by the tracker.
     */
    public void ImportMessage(Peer peer, byte[] messageData) {
        // read in the list of files from a peer, updating the list for this peer
        RefreshLists();
    }

    /**
     * Creates the Tracker Registration message as it would appear on the wire. Expected to be called by a client.
     *
     * @param filenames List of filenames to include in the registration message
     * @return bytestream of the registration message
     */
    public byte[] CreateMessage(String[] filenames) {

        return null;
    }

    /*
     * Updates the internal lists to remove list entries that have timed out. Expected to be called by tracker-used functions.
     */
    private void RefreshLists() {

    }
}
