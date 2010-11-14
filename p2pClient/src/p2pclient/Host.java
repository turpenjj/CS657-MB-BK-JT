/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class Host {
    ChunkManager[] chunkManagers;
    PeerManager peerManager;
    RequestingClient[] requestingClients;
    ServingClient servingClient;
    int listeningPort;
    String shareFolder;

    Host(int servingClientListeningPort, String directory) {
        listeningPort = servingClientListeningPort;
        shareFolder = directory;
    }

    /*
     * Description:
     *   Returns a list of all Files there are currently outstanding download
     *   requests for
     */
    public String[] GetCurrentDownloads(){
        return null;
    }

    /*
     * Description:
     *   Returns the chunk information for all chunks associated with a given
     *   file.  Returns null if the given file is not known.
     */
    public ChunkInfo[] GetFileChunkInfo(String filename) {
        return null;
    }

    /*
     * Description:
     *   Starts a download for the given file
     */
    public void StartDownload(String filename) {


    }

    /*
     * Description:
     *   Searches the tracker for a given file
     */
    public void Search(String filename) {
        
    }
}
