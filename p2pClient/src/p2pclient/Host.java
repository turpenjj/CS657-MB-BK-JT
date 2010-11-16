/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;
import java.io.*;

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

    public Host(int servingClientListeningPort, String directory) {
        runner = null;
        listeningPort = servingClientListeningPort;
        shareFolder = directory;
        chunkManagers = new ChunkManager[2];
        chunkManagers[0] = new ChunkManager("fu");
        chunkManagers[1] = new ChunkManager("man");
        servingClient = new ServingClient(listeningPort, chunkManagers, peerManager);

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
        String[] currentDownloads = new String[5];
        currentDownloads[0] = "download1.foo";
        currentDownloads[1] = "download2.foo";
        currentDownloads[2] = "download3.foo";
        currentDownloads[3] = "download4.foo";
        currentDownloads[4] = "download5.foo";
        return currentDownloads;
    }

    public synchronized String[] GetCurrentUploads() {
        String[] currentUploads = new String[1];
        currentUploads[0] = "upload1.foo";
        return currentUploads;
    }

    /*
     * Description:
     *   Returns the chunk information for all chunks associated with a given
     *   file.  Returns null if the given file is not known.
     */
    public synchronized ChunkInfo[] GetFileChunkInfo(String filename) {
        ChunkInfo[] allChunks = new ChunkInfo[10];
        byte[] hash = new byte[20];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(Util.IntToByteArray(i), 0, hash, 0, 4);
            System.arraycopy(Util.IntToByteArray(2*i), 0, hash, 4, 4);
            System.arraycopy(Util.IntToByteArray(3*i), 0, hash, 8, 4);
            System.arraycopy(Util.IntToByteArray(4*i), 0, hash, 12, 4);
            System.arraycopy(Util.IntToByteArray(5*i), 0, hash, 16, 4);
            allChunks[i] = new ChunkInfo(i, hash, 0);
            byte[] IPinBytes = Util.IntToByteArray(0xc0a80101 + i);
            try {
                allChunks[i].receivedFrom = new Peer(InetAddress.getByAddress(IPinBytes), 1000 + i);
            } catch ( UnknownHostException e ) {
                Util.DebugPrint(DbgSub.HOST, "Unknown host: " + e);
            }
        }
        return allChunks;
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
    public synchronized Peer[] Search(String filename) {
        return null;
    }

    private void AddFilesFromDirectory(String directory) {
        File dir = new File(directory);
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
        }
    }
}
