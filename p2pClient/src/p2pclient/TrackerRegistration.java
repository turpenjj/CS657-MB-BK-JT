package p2pclient;

import java.io.*;
import java.net.*;
import java.io.FileFilter;

/**
 * This handles tracking of all registered peers and the list of files they
 * currently have chunks for. It provides public methods for searching the lists
 * and adding new items to the list. It also handles timing out entries from the
 * lists. The structure of the message payload on the wire is:
 *
 * uint32_t listeningPort; // port the peer is listening for requests on
 * char[] filenames; // null-terminated list of filenames peer is sharing
 *
 * We don't have a process/thread for handling timeouts. We simply refresh the
 * lists at the end of each external entry point.
 *
 * @author Jeremy
 */
public class TrackerRegistration {
    // TODO: switch these back to private when we are done using it for testing
    // For a peer, this will only have one element. For the Tracker it will
    // likely have many.
    public RegisteredPeer[] registeredPeers = {null};
    // Used for peer only, not tracker
    public Peer peer;
    private int BASE_MESSAGE_SIZE = 4; // listeningPort (4)
    // TODO: switch this back to private when we are done using it for testing
    public long PEER_TIMEOUT_MSEC = 2 * 60 * 1000;

    /**
     * Peer constructor. Called when creating the registration, followed by a
     * call to AddFilesFromDirectory, followed by a call to ExportMessagePayload.
     * @param listeningPort Port to listen for requests from other peers on
     */
    TrackerRegistration(int listeningPort) throws Exception {
        InetAddress inetAddr = InetAddress.getLocalHost();

        this.peer = new Peer(inetAddr, listeningPort);
    }

    /**
     * Tracker constructor. Called to set up a single registration tracker for
     * the tracker. As new registration messages come in, ImportMessagePayload
     * is called.
     */
    TrackerRegistration() {
        // intentionally empty
    }

    /*
     * Allows the tracker to search for a list of Peers that have chunks to the requested file.
     */
    public synchronized Peer[] Search(String filename) {
        Peer[] retList = null;

        RefreshLists();

        // iterate through the list and return any matches
        for (RegisteredPeer regPeer : this.registeredPeers) {
            for (String file : regPeer.files) {
               if (file.compareToIgnoreCase(filename) == 0) {
                   retList = AddToPeerList(retList, regPeer.peer);
               }
            }
        }
        
        return retList;
    }

    private synchronized Peer[] AddToPeerList(Peer[] list, Peer peer) {
        Peer[] temp;

        if (list == null || list[0] == null) {
            temp = new Peer[1];
            temp[0] = peer;
            
            return temp;
        }
        
        temp = new Peer[list.length + 1];

        temp[0] = peer;
        System.arraycopy(list, 0, temp, 1, list.length);
        
        return temp;
    }

    /**
     * Adds files from the given directory to the list to be sent with a tracker registration message. Expected to be called by a client.
     * @param path Directory to search for files in
     */
    public synchronized void AddFilesFromDirectory(String path) {
        // TODO: remove this test code
        if (path.compareTo("Test") == 0) {
            String[] filenames = {"Test1", "Test2", "Test3", "Test4", "Test5"};
            this.registeredPeers[0] = new RegisteredPeer(this.peer, filenames);

            return;
        } else if (path.compareTo("Set") == 0) {
            String[] filenames = {"Set1", "Set2", "Set3", "Set4", "Set5", "Set6", "Set7"};
            this.registeredPeers[0] = new RegisteredPeer(this.peer, filenames);

            return;
        }

        File dir = new File(path);
        FileFilter filter = new RealFileFilter();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(filter);
            String[] filenames = new String[0];
            String[] temp;

            for (File file : files) {
                temp = new String[1 + filenames.length];
                temp[0] = file.getName();
                System.arraycopy(filenames, 0, temp, 1, filenames.length);
                filenames = temp;
            }

            this.registeredPeers[0] = new RegisteredPeer(this.peer, filenames);
        } else {
            this.registeredPeers[0] = new RegisteredPeer(this.peer, null);
        }
    }

    /*
     * Processes the Tracker Registration message is it appears on the wire. Expected to be called by the tracker.
     */
    public synchronized void ImportMessage(Peer peer, byte[] messageData) {
        int currentIndex = 0;
        int[] nextIndex = {0};
        int numStrings = 0;
        int startOfStringsIndex = 0;
        String[] strings;
        int i;

        RefreshLists();

        peer.listeningPort = Util.ByteArrayToInt(messageData, currentIndex);
        currentIndex += 4;
        startOfStringsIndex = currentIndex;

        while (Util.ExtractNullTerminatedString(messageData, currentIndex, nextIndex) != null) {
            numStrings++;
            currentIndex = nextIndex[0];
        }
        
        strings = new String[numStrings];
        currentIndex = startOfStringsIndex;
        for ( i = 0; i < numStrings; i++) {
            strings[i] = Util.ExtractNullTerminatedString(messageData, currentIndex, nextIndex);
            currentIndex = nextIndex[0];
        }

        this.AddPeer(peer, strings);
    }

    /**
     * Creates the Tracker Registration message as it would appear on the wire.
     * Expected to be called by a client.
     *
     * @return bytestream of the registration message
     */
    public synchronized byte[] ExportMessagePayload() {
        String built = "";
        byte[] messageData;
        int currentIndex = 0;

        for (String file : this.registeredPeers[0].files) {
            built = built.concat(file + '\0');
        }
        // strip the trailing null since InsertNullTerminatedString will add one for us
        if (this.registeredPeers[0].files.length != 0) {
            built = built.substring(0, built.length() - 1);
        }

        // + 1 is for the null InsertNullTerminatedString will add for us
        messageData = new byte[this.BASE_MESSAGE_SIZE + built.length() + 1];

        currentIndex = Util.IntToByteArray(messageData, currentIndex, this.peer.listeningPort);

        currentIndex = Util.InsertNullTerminatedString(messageData, currentIndex, built);

        return messageData;
    }

    private synchronized void AddPeer(Peer peer, String[] filenames) {
        int i = 0;
        RegisteredPeer[] tempRegisteredPeers;

        if (this.registeredPeers.length != 0 && this.registeredPeers[0] != null) {
            for (RegisteredPeer loopPeer : this.registeredPeers) {
                if (loopPeer.peer.clientIp.getHostAddress().equals(peer.clientIp.getHostAddress()) &&
                        loopPeer.peer.listeningPort == peer.listeningPort) {
                    break;
                }
                i++;
            }
        }

        if (i != this.registeredPeers.length) {
            this.registeredPeers[i] = new RegisteredPeer(peer, filenames);
        } else {
            tempRegisteredPeers = new RegisteredPeer[i + 1];
            tempRegisteredPeers[0] = new RegisteredPeer(peer, filenames);

            System.arraycopy(this.registeredPeers, 0, tempRegisteredPeers, 1, this.registeredPeers.length);
            this.registeredPeers = tempRegisteredPeers;
        }
    }

    /*
     * Updates the internal lists to remove list entries that have timed out.
     * Expected to be called by tracker-used functions.
     */
    public synchronized void RefreshLists() {
        // TODO: switch this function back to private once we are done using it for testing
        long currentTime = Util.GetCurrentTime();
        int currentIndex = 0;
        RegisteredPeer[] tempRegisteredPeers = new RegisteredPeer[0];

        if (this.registeredPeers == null) {
            return;
        }
        if (this.registeredPeers[0] == null) {
            return;
        }
        
        for (RegisteredPeer loopPeer : this.registeredPeers) {
            if (loopPeer.timestampMsec + this.PEER_TIMEOUT_MSEC < currentTime) {
                tempRegisteredPeers = new RegisteredPeer[this.registeredPeers.length - 1];
                System.arraycopy(this.registeredPeers, 0, tempRegisteredPeers, 0, currentIndex);
                System.arraycopy(this.registeredPeers, currentIndex + 1, tempRegisteredPeers, currentIndex, tempRegisteredPeers.length - currentIndex);
                this.registeredPeers = tempRegisteredPeers;
                continue;
            }
            currentIndex++;
        }
    }

    public synchronized String toString() {
        String string = "Registered Peers:\n";

        if (this.registeredPeers != null && this.registeredPeers.length != 0 && this.registeredPeers[0] != null) {
            for (RegisteredPeer peer : this.registeredPeers) {
                string = string.concat(peer.toString() + "\n");
            }
        }

        return string;
    }
}
