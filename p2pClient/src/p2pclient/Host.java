/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;
import java.io.*;
import java.util.Random;

/**
 *
 * @author Matt
 */
public class Host implements Runnable{
    int MAX_NUMBER_FILES = 1024;
    private Thread runner;
    ChunkManager[] chunkManagers;
    PeerManager peerManager;
    RequestingClient[] requestingClients;
    ServingClient servingClient;
    int listeningPort;
    String shareFolder;
    InetAddress trackerIP;

    public Host(int servingClientListeningPort, String directory) {
        runner = null;
        listeningPort = servingClientListeningPort;
        shareFolder = directory + "\\";
        chunkManagers = PopulateChunkManagers();
        peerManager = new PeerManager();
        servingClient = new ServingClient(listeningPort, chunkManagers, peerManager);
        trackerIP = null;
    }

    public Host(int servingClientListeningPort, String directory, String trackerIp) throws UnknownHostException {
        runner = null;
        listeningPort = servingClientListeningPort;
        shareFolder = directory + "\\";
        chunkManagers = PopulateChunkManagers();
        peerManager = new PeerManager();
        servingClient = new ServingClient(listeningPort, chunkManagers, peerManager);
        trackerIP = InetAddress.getByName(trackerIp);
    }

    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        Util.DebugPrint(DbgSub.HOST, "Startng a new host, listening for connections on port " + listeningPort);
        ChunkManager[] temp = new ChunkManager[3];
        System.arraycopy(chunkManagers, 0, temp, 0, 2);
        chunkManagers = temp;
        chunkManagers[2] = new ChunkManager("chu");
        servingClient.UpdateChunkManagers(chunkManagers);
        //Start our listener so we can serve out file chunks upon request
        servingClient.start();
        MessageSend messageSender = new MessageSend();

        try {
            TrackerRegistration trackerRegistration = new TrackerRegistration(listeningPort);

            trackerRegistration.AddFilesFromDirectory(shareFolder);

            byte[] registrationMessageData = trackerRegistration.ExportMessagePayload();

            Peer tracker = new Peer(InetAddress.getByName("192.168.100.202"), Tracker.TRACKER_LISTENING_PORT);

            messageSender.SendCommunication(tracker, PacketType.TRACKER_REGISTRATION, Tracker.TRACKER_LISTENING_PORT, registrationMessageData);
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.HOST, "Caught exception " + e);
        }

        for ( ;; ) {

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
            if (chunkManager.NeededChunks() != null || chunkManager.DownloadingChunks() != null ) {
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
                    currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat("" + peer.chunksWeSent[i][j]);
                    if ( j != peer.chunksWeSent[i].length - 1) {
                        currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat(", ");
                    }
                }
                currentUploads[currentUploads.length-1] = currentUploads[currentUploads.length-1].concat(" sent to " + peer.clientIp);
            }
        }
        return currentUploads;
    }

    /*
     * Description:
     *   Returns the chunk information for all chunks associated with a given
     *   file.  Returns null if the given file is not known.
     */
    public synchronized ChunkInfo[] GetFileChunkInfo(String filename) {
        ChunkManager chunkManager = FindChunkManager(filename);

        if ( chunkManager != null ) {
            return chunkManager.GetChunkInfo();
        }
        return null;
    }

    /*
     * Starts a download for the given file
     */
    public synchronized void StartDownload(String filename) {
        Peer[] peerList = peerManager.GetAllPeersSharingFile(filename);
        for ( Peer peer : peerList ) {
            StartDownload(filename, peer);
        }
    }

    /*
     * Starts downloading a file from a specific peer
     */
    public synchronized void StartDownload(String filename, Peer peer) {
        ChunkManager chunkManager = FindChunkManager(filename);

        //If we couldn't find the chunk manager for this file, we can't download it!
        if ( chunkManager != null ) {
            RequestingClient requestingClient = new RequestingClient(peer, filename, chunkManager, peerManager);
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
    public synchronized Peer[] Search(String filename) throws InterruptedException {
        MessageSend messageSender = new MessageSend();
        PacketType[] acceptedPacketType = {PacketType.TRACKER_QUERY_RESPONSE};
        MessageReceive queryMessageReceive = new MessageReceive(this.listeningPort, acceptedPacketType, false);
        queryMessageReceive.start();
        byte[] receivedMessageData;
        Peer[] receivedPeer = new Peer[1];
        int[] receivedSessionId = new int[1];
        PacketType[] receivedPacketType = new PacketType[1];
        Random random = new Random();
        int sessionId = random.nextInt();
        TrackerQueryResponse trackerQueryResponse;
        Peer trackerPeer = new Peer(trackerIP, Tracker.TRACKER_LISTENING_PORT);

        TrackerQuery trackerQuery = new TrackerQuery(filename, this.listeningPort);
        messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_QUERY, Tracker.TRACKER_LISTENING_PORT, trackerQuery.ExportQuery());

        Thread.sleep(1000);

        receivedMessageData = queryMessageReceive.GetMessage(sessionId, acceptedPacketType, receivedPeer, receivedPacketType, receivedSessionId);
            if (receivedMessageData != null) {
                trackerQueryResponse = new TrackerQueryResponse();
                trackerQueryResponse.ImportResponse(receivedMessageData);

                if ( trackerQueryResponse.peerList != null ) {
                    queryMessageReceive.Stop();
                    return trackerQueryResponse.peerList;
                } else {
                    queryMessageReceive.Stop();
                }
            } else {
                queryMessageReceive.Stop();
                return null;
            }
        return null;
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
            File file = new File(path + filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] byteBuffer = new byte[torrent.CHUNK_SIZE];
            int bytesRead;

            ChunkManager chunkManager = new ChunkManager(torrent.filename);
            chunkManager.chunkList = new FileChunk[torrent.numChunks];
            for ( int i = 0; i < torrent.numChunks; i++ ) {
                bytesRead = fileInputStream.read(byteBuffer);
                chunkManager.chunkList[i] = new FileChunk(torrent.chunks[i].chunkNumber, torrent.chunks[i].hash, torrent.chunks[i].status, null);
                chunkManager.chunkList[i].chunk = new byte[bytesRead];
                System.arraycopy(byteBuffer, 0, chunkManager.chunkList[i].chunk, 0, bytesRead);
                chunkManager.chunkList[i].chunkInfo = torrent.chunks[i];
            }
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
