package p2pclient;

import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Top-level class representing a peer in the p2p system. Responsible for both
 * client (requesting file chunks from other peers) and server (sending file
 * chunks to other peers) aspects.
 *
 * @author Matt
 */
public class Host implements Runnable{
    /** @todo Unused and can be removed */
    int MAX_NUMBER_FILES = 1024;
    /** Top-level thread */
    private Thread runner;
    /** Array of Chunk Managers, one per file being shared or downloaded */
    ChunkManager[] chunkManagers;
    /** Manages all known peers */
    PeerManager peerManager;
    /** Array of Requesting Clients (threads and sockets for sending requests and awaiting responses) */
    RequestingClient[] requestingClients;
    /** Single serving client (single thread and socket) for responding to requests from other peers */
    ServingClient servingClient;
    /** Port on which the ServingClient is listening */
    int listeningPort;
    /** Path containing the files being shared */
    String shareFolder;
    /** Address of the Tracker to connect to */
    InetAddress trackerIP;
//    boolean isTracker;
//    Tracker tracker;
    /** Peer object representing the Tracker */
    Peer peerTracker;
    /** Time at which we need to re-register out torrents with the Tracker */
    private long torrentBroadcastTimeout;
    /** Interval at which we need to re-register out torrents with the Tracker */
    public static int TORRENT_BROADCAST_FREQUENCY = 20000; //20 seconds
    /** Time at which we need to re-register ourself as a peer with the Tracker */
    private long trackerRegistrationBroadcastTimeout;
    /** Interval at which we need to re-register ourself as a peer with the Tracker */
    public static int TRACKER_REGISTRATION_FREQUENCY = 40000; //40 seconds
    /** Time we are willing to wait on a response before assuming it is lost */
    public static int SEARCH_TIMEOUT = 10000; //10 seconds

    public Host(int servingClientListeningPort, String directory, String trackerIp) throws UnknownHostException {
        runner = null;
        listeningPort = servingClientListeningPort;
        shareFolder = directory + "\\";
        chunkManagers = PopulateChunkManagers();
        peerManager = new PeerManager();
        servingClient = new ServingClient(listeningPort, chunkManagers, peerManager);
        trackerIP = InetAddress.getByName(trackerIp);
//        isTracker = false;
//        tracker = null;
        Util.DebugPrint(DbgSub.HOST, trackerIp);
        peerTracker = new Peer(this.trackerIP, Tracker.TRACKER_LISTENING_PORT);
        torrentBroadcastTimeout = Util.GetCurrentTime(); //broadcast our torrents when we first spin up
        trackerRegistrationBroadcastTimeout = Util.GetCurrentTime(); //broadcast our registrationg when we first spin up
    }

    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        Util.DebugPrint(DbgSub.HOST, "Startng a new host, listening for connections on port " + listeningPort);
        //Start our listener so we can serve out file chunks upon request
        servingClient.start();

        for ( ;; ) {
            //Check if it's time to broadcast our tracker registration
            if ( trackerRegistrationBroadcastTimeout < Util.GetCurrentTime() ) {
                BroadcastTrackerRegistration();
                //update broadcast timeout
                trackerRegistrationBroadcastTimeout = Util.GetCurrentTime() + TRACKER_REGISTRATION_FREQUENCY;
            }

            //Check if it's time to broadcast our torrent knowledge
            if ( torrentBroadcastTimeout < Util.GetCurrentTime() ) {
                BroadcastTorrents();
                //update broadcast timeout
                torrentBroadcastTimeout = Util.GetCurrentTime() + TORRENT_BROADCAST_FREQUENCY;
            }

        }

    }

    /*
     * Description:
     *   Returns a list of all Files there are currently outstanding download
     *   requests for
     */
    public synchronized String[] GetCurrentDownloads(){
        String[] currentDownloads = null;
        if ( chunkManagers == null ) {
            return null;
        }
        for ( ChunkManager chunkManager : chunkManagers ) {
            if ( chunkManager.downloadStarted && (chunkManager.NeededChunks() != null || chunkManager.DownloadingChunks() != null) ) {
                if ( currentDownloads == null ) {
                    currentDownloads = new String[1];
                } else {
                    String[] tempList = new String[currentDownloads.length + 1];
                    System.arraycopy(currentDownloads, 0, tempList, 0, currentDownloads.length);
                    currentDownloads = tempList;
                }
                currentDownloads[currentDownloads.length - 1] = chunkManager.filename;
            }
        }
        return currentDownloads;
    }

    public synchronized String[] GetCurrentUploads() {
        String[] currentUploads = null;
        if ( peerManager.peerList == null ) {
            return null;
        }
        for ( Peer peer : peerManager.peerList ) {
            if ( peer.filesWeSent != null ) {
                for ( int i = 0; i < peer.filesWeSent.length; i++ ) {
                    if ( currentUploads == null  ) {
                        currentUploads = new String[1];
                    } else {
                        String[] tempList = new String[currentUploads.length + 1];
                        System.arraycopy(currentUploads, 0, tempList, 0, currentUploads.length);
                        currentUploads = tempList;
                    }
                    currentUploads[currentUploads.length - 1] = peer.filesWeSent[i] + " {";
                    for ( int j = 0; j < peer.chunksWeSent[i].length; j++ ) {
                        currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat("" + peer.chunksWeSent[i][j] + "}");
                        if ( j != peer.chunksWeSent[i].length - 1) {
                            currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat(", ");
                        }
                    }
                    currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat(" sent to " + peer.clientIp);
                }
            }
        }
        return currentUploads;
    }

    /*
     * Description:
     *   Returns the chunk information for all chunks associated with a given
     *   file.  Returns null if the given file is not known.
     */
    public ChunkInfo[] GetFileChunkInfo(String filename) {
        ChunkManager chunkManager = FindChunkManager(filename);

        if ( chunkManager != null ) {
            return chunkManager.GetChunkInfo();
        }
        return null;
    }

    /*
     * Starts a download for the given file
     */
    public void StartDownload(String filename) {
        //Get the torrent file from the tracker if we don't already have it.
        Peer[] peerList = peerManager.GetAllPeersSharingFile(filename);
        for ( Peer peer : peerList ) {
            StartDownload(filename, peer);
        }
    }

    private ChunkManager RequestTorrent(String filename) {
        ChunkManager chunkManager = null;

        Util.DebugPrint(DbgSub.HOST, "Need to request torrent");
        int DOWNLOAD_TIMEOUT = 10000;
        long downloadTimeout = Util.GetCurrentTime() + DOWNLOAD_TIMEOUT;
        int torrentListeningPort = Util.GetRandomHighPort(new java.util.Random());
        PacketType[] acceptedPacketType = {PacketType.TRACKER_TORRENT_RESPONSE};
        MessageReceive torrentReceiver = new MessageReceive(torrentListeningPort, acceptedPacketType, true);
        torrentReceiver.start();
        MessageSend torrentRequestor = new MessageSend();
        TrackerTorrentQuery torrentRequest = new TrackerTorrentQuery(filename, torrentListeningPort);
        torrentRequestor.SendCommunication(peerTracker, PacketType.TRACKER_TORRENT_QUERY, torrentListeningPort, torrentRequest.ExportQuery());
        Peer[] receivedPeer = new Peer[1];
        int[] receivedSessionId = new int[1];
        PacketType[] receivedPacketType = new PacketType[1];
        while ( Util.GetCurrentTime() < downloadTimeout ) {
            //Request the torrent file
            byte[] receivedMessage = torrentReceiver.GetMessage(torrentListeningPort, acceptedPacketType, receivedPeer,  receivedPacketType, receivedSessionId);
            if ( receivedMessage != null ) {
                TrackerTorrentResponse torrentResponse = new TrackerTorrentResponse();
                torrentResponse.ImportMessagePayload(receivedMessage);
                if ( torrentResponse.torrent.numChunks > 0 ) {
                    chunkManager = new ChunkManager(torrentResponse.torrent, shareFolder);
                    AddChunkManager(chunkManager);
                    Util.DebugPrint(DbgSub.HOST, "Added a chunk manager");
                }
                break;
            }
            Thread.yield();
        }
        torrentReceiver.Stop();
        return chunkManager;
    }

    /*
     * Starts downloading a file from a specific peer
     */
    public void StartDownload(String filename, Peer peer) {
        ChunkManager chunkManager = FindChunkManager(filename);
        Util.DebugPrint(DbgSub.HOST, "Starting to download " + filename + " from " + peer.clientIp);
        if ( chunkManager == null ) {
            chunkManager = RequestTorrent(filename);
        }
        //If we couldn't find the chunk manager for this file, we can't download it!
        if ( chunkManager != null ) {
            Util.DebugPrint(DbgSub.HOST, "Started downloading!");
            chunkManager.downloadStarted = true;
            RequestingClient requestingClient = new RequestingClient(peer, filename, chunkManager, peerManager);
            requestingClient.start();
            AddRequestingClientToList(requestingClient);
        }
    }

    /*
     * Returns the ChunkManager for the given file.  If it doesn't exist, returns null
     */
    private synchronized ChunkManager FindChunkManager(String filename) {
        if ( chunkManagers != null ) {
            for (int i = 0; i < chunkManagers.length; i++) {
                if ( chunkManagers[i].filename.equals(filename)) {
                    return chunkManagers[i];
                }
            }
        }
        return null;
    }

    private synchronized void AddRequestingClientToList(RequestingClient requestingClient) {
        if ( requestingClients == null ) {
            requestingClients = new RequestingClient[1];
        } else {
            RequestingClient[] tempList = new RequestingClient[requestingClients.length + 1];
            System.arraycopy(requestingClients, 0, tempList, 0, requestingClients.length);
            requestingClients = tempList;
        }
        requestingClients[requestingClients.length - 1] = requestingClient;
    }

    /*
     * Description:
     *   Searches the tracker for a given file
     */
    public Peer[] Search(String filename) throws InterruptedException {
        int searchListeningPort = Util.GetRandomHighPort(new Random());
        MessageSend messageSender = new MessageSend();
        PacketType[] acceptedPacketType = {PacketType.TRACKER_QUERY_RESPONSE};
        MessageReceive queryMessageReceive = new MessageReceive(searchListeningPort, acceptedPacketType, true);
        queryMessageReceive.start();
        byte[] receivedMessageData;
        Peer[] receivedPeer = new Peer[1];
        int[] receivedSessionId = new int[1];
        PacketType[] receivedPacketType = new PacketType[1];
        Random random = new Random();
        int sessionId = random.nextInt();
        TrackerQueryResponse trackerQueryResponse;
        Peer trackerPeer = new Peer(trackerIP, Tracker.TRACKER_LISTENING_PORT);

        TrackerQuery trackerQuery = new TrackerQuery(filename, searchListeningPort);
        Util.DebugPrint(DbgSub.HOST, trackerPeer.clientIp);
        messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_QUERY, sessionId, trackerQuery.ExportQuery());

        long searchTimeout = Util.GetCurrentTime() + SEARCH_TIMEOUT;
        while ( Util.GetCurrentTime() < searchTimeout ) {
            receivedMessageData = queryMessageReceive.GetMessage(sessionId, acceptedPacketType, receivedPeer, receivedPacketType, receivedSessionId);
            if (receivedMessageData != null) {
                trackerQueryResponse = new TrackerQueryResponse();
                trackerQueryResponse.ImportResponse(receivedMessageData);

                if ( trackerQueryResponse.peerList != null ) {
                    queryMessageReceive.Stop();
                    for ( Peer peer : trackerQueryResponse.peerList ) {
                        peerManager.UpdatePeer(peer);
                    }
                    return trackerQueryResponse.peerList;
                } else {
                    queryMessageReceive.Stop();
                }
            }
            Thread.yield();
        }
        return null;
    }

    private void BroadcastTrackerRegistration() {
        try {
            Util.DebugPrint(DbgSub.HOST, "Broadcasting our registrationg to the tracker");
            MessageSend messageSender = new MessageSend();
            TrackerRegistration trackerRegistration = new TrackerRegistration(listeningPort);

            trackerRegistration.AddFilesFromDirectory(shareFolder);

            byte[] registrationMessageData = trackerRegistration.ExportMessagePayload();

            messageSender.SendCommunication(peerTracker, PacketType.TRACKER_REGISTRATION, Util.GetRandomHighPort(new java.util.Random()), registrationMessageData);

        } catch (Exception e) {
            Util.DebugPrint(DbgSub.HOST, "Caught exception " + e);
        }
    }

    private synchronized String[] GetAllFilesInSharedDir() {
        File dir = new File(shareFolder);
        FileFilter filter = new RealFileFilter();
        String[] filenames = new String[0];
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(filter);
            String[] temp;

            for (File file : files) {
                temp = new String[1 + filenames.length];
                temp[0] = file.getName();
                System.arraycopy(filenames, 0, temp, 1, filenames.length);
                filenames = temp;
            }
        }

        return filenames;

    }

    private void BroadcastTorrents() {
        MessageSend messageSender = new MessageSend();
        Util.DebugPrint(DbgSub.HOST, "Attempting to broadcast torrents");

        Torrent torrent;
        String[] allTorrents = GetAllFilesInSharedDir();
        int torrentQueryPort = Util.GetRandomHighPort(new java.util.Random());
        PacketType[] acceptedPacketType = {PacketType.TRACKER_TORRENT_RESPONSE};
        MessageReceive torrentQueryReceive = new MessageReceive(torrentQueryPort, acceptedPacketType, true);
        if ( allTorrents == null ) {
            return;
        }
        torrentQueryReceive.start();

        int torrentIndex = 0;
        //Request all torrents, we'll only broadcast the torrents the tracker doesn't
        //have or doesn't respond to
        for ( String fileToTorrent : allTorrents ) {
            TrackerTorrentQuery trackerTorrentQuery = new TrackerTorrentQuery(fileToTorrent, torrentQueryPort);
            messageSender.SendCommunication(peerTracker, PacketType.TRACKER_TORRENT_QUERY, (torrentQueryPort+torrentIndex), trackerTorrentQuery.ExportQuery());
            torrentIndex++;
        }
        try {
            //Wait for 5 seconds before checking the responses to give the tracker time to respond
            Thread.sleep(5000);
            for ( int i = 0; i < torrentIndex; i ++ ) {
                //Request a message for each Torrent. If we don't have a message, that torrent needs to be broadcast
                boolean shouldBroadcast = false;
                PacketType[] receivedPacketType = new PacketType[1];

                byte[] messageReceived = torrentQueryReceive.GetMessage(torrentQueryPort+i, acceptedPacketType, null, null, null);
                if ( messageReceived != null ) {
                    TrackerTorrentResponse trackerTorrentResponse = new TrackerTorrentResponse();
                    trackerTorrentResponse.ImportMessagePayload(messageReceived);
                    if ( trackerTorrentResponse.torrent == null || trackerTorrentResponse.torrent.numChunks == 0 ) {
                        shouldBroadcast = true;
                    }
                } else {
                    shouldBroadcast = true;
                }
                //No message received or no torrent received, broadcast out torrent
                if ( shouldBroadcast ) {
                    Util.DebugPrint(DbgSub.HOST, "Broadcasting torrent information for " + allTorrents[i]);
                    torrent = new Torrent(shareFolder, allTorrents[i]);
                    Util.DebugPrint(DbgSub.HOST, "  has " + torrent.numChunks + " chunks");
                    TrackerTorrentRegistration torrentRegistration = new TrackerTorrentRegistration(torrent);
                    byte[] torrentRegistrationData = torrentRegistration.ExportMessagePayload();
                    messageSender.SendCommunication(peerTracker, PacketType.TRACKER_TORRENT_REGISTRATION, Util.GetRandomHighPort(new java.util.Random()), torrentRegistrationData);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Host.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void AddChunkManager(ChunkManager chunkManager) {
        if ( chunkManagers == null ) {
            chunkManagers = new ChunkManager[1];
        } else {
            ChunkManager[] tempList = new ChunkManager[chunkManagers.length + 1];
            System.arraycopy(chunkManagers, 0, tempList, 0, chunkManagers.length);
            chunkManagers = tempList;
        }
        chunkManagers[chunkManagers.length - 1] = chunkManager;
    }

    private synchronized ChunkManager[] PopulateChunkManagers() {
        String[] filesToTorrent = GetTorrentFilesFromDirectory(shareFolder);
        ChunkManager[] storedChunkManagers = new ChunkManager[filesToTorrent.length];

        int managerIndex = 0;
        for ( String torrentFile : filesToTorrent ) {
            Torrent tempTorrent = null;
            try {
                tempTorrent = new Torrent(shareFolder, torrentFile);
            } catch ( Exception e ) {
                Util.DebugPrint(DbgSub.HOST, e);
            }
            storedChunkManagers[managerIndex++] = CreateChunkManagerFromTorrent(shareFolder, torrentFile, tempTorrent);
        }
        Util.DebugPrint(DbgSub.HOST, "Loaded " + storedChunkManagers.length + " torrents");
        return storedChunkManagers;
    }

    private synchronized ChunkManager CreateChunkManagerFromTorrent(String path, String filename, Torrent torrent) {
        try {
            byte[] byteBuffer = null;
            int bytesRead;
            FileInputStream fileInputStream = null;
            if ( path != null && filename != null ) {
                File file = new File(path + filename);
                fileInputStream = new FileInputStream(file);
                byteBuffer = new byte[torrent.CHUNK_SIZE];
            }

            ChunkManager chunkManager = new ChunkManager(torrent.filename, shareFolder);
            chunkManager.chunkList = new FileChunk[torrent.numChunks];
            for ( int i = 0; i < torrent.numChunks; i++ ) {
                chunkManager.chunkList[i] = new FileChunk(torrent.chunks[i].chunkNumber, torrent.chunks[i].hash, torrent.chunks[i].status, null);
                chunkManager.chunkList[i].chunkInfo = torrent.chunks[i];

                if ( fileInputStream != null ) {
                    bytesRead = fileInputStream.read(byteBuffer);
                    chunkManager.chunkList[i].chunk = new byte[bytesRead];
                    System.arraycopy(byteBuffer, 0, chunkManager.chunkList[i].chunk, 0, bytesRead);
                }
            }
            Util.DebugPrint(DbgSub.HOST, "Created a chunk manager based off torrent: " + chunkManager + " filename " + chunkManager.filename);
            return chunkManager;
        } catch ( IOException e ) {
        }
        return null;
    }

    /*
     * This will return all the *.torrent files in the directory
     */
    private String[] GetTorrentFilesFromDirectory(String directory) {
        File dir = new File(directory);
        FileFilter filter = new RealFileFilter();
        if ( dir != null && dir.exists() && dir.isDirectory() ) {
            File[] files = dir.listFiles(filter);
            String[] filenames = new String[0];
            String[] temp;

            for ( File file : files ) {
                Util.DebugPrint(DbgSub.HOST, "Read in file for torrent" + file.getName());
                temp = new String[1 + filenames.length];
                temp[0] = file.getName();
                System.arraycopy(filenames, 0, temp, 1, filenames.length);
                filenames = temp;
            }
            return filenames;
        }
        return null;
    }
}
