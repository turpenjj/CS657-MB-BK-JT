/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;


/**
 *
 * @author Matt
 */
public class Host extends Util implements Runnable{
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
        System.out.println("Startng a new host, listening for connections on port " + listeningPort);
        ChunkManager[] temp = new ChunkManager[3];
        System.arraycopy(chunkManagers, 0, temp, 0, 2);
        chunkManagers = temp;
        chunkManagers[2] = new ChunkManager("chu");
        servingClient.UpdateChunkManagers(chunkManagers);
        //Start our listener so we can serve out file chunks upon request
        servingClient.start();
    }


    /*
     * Description:
     *   Returns a list of all Files there are currently outstanding download
     *   requests for
     */
    public String[] GetCurrentDownloads(){
        String[] currentDownloads = new String[5];
        currentDownloads[0] = "download1.foo";
        currentDownloads[1] = "download2.foo";
        currentDownloads[2] = "download3.foo";
        currentDownloads[3] = "download4.foo";
        currentDownloads[4] = "download5.foo";
        return currentDownloads;
    }

    /*
     * Description:
     *   Returns the chunk information for all chunks associated with a given
     *   file.  Returns null if the given file is not known.
     */
    public ChunkInfo[] GetFileChunkInfo(String filename) {
        ChunkInfo[] allChunks = new ChunkInfo[10];
        byte[] hash = new byte[20];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(IntToByteArray(i), 0, hash, 0, 4);
            System.arraycopy(IntToByteArray(2*i), 0, hash, 4, 4);
            System.arraycopy(IntToByteArray(3*i), 0, hash, 8, 4);
            System.arraycopy(IntToByteArray(4*i), 0, hash, 12, 4);
            System.arraycopy(IntToByteArray(5*i), 0, hash, 16, 4);
            allChunks[i] = new ChunkInfo(i, hash, 0);
            allChunks[i].receivedFrom = new Peer(0xc0a80101 + i, 1000 + i);
        }
        return allChunks;
    }

    /*
     * Starts a download for the given file
     */
    public void StartDownload(String filename) {


    }

    /*
     * Starts downloading a file from a specific peer
     */
    public void StartDownload(String filename, Peer peer) {
        
    }

    /*
     * Description:
     *   Searches the tracker for a given file
     */
    public Peer[] Search(String filename) {
        return null;
    }
}
