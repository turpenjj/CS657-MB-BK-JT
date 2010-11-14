/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class Peer {
    int clientIp;
    int listeningPort;
    String[] fileList;
    int[][] chunkList; //[fileIndex][chunkIndex]
    int creditForThem; //The credit associated with this peer
    int creditForUs; //An approximation of the credit this peer has for us

    Peer(int ip, int port) {
        clientIp = ip;
        listeningPort = port;
    }

    /*
     * Description:
     *   Returns the list of chunks this peer claims to have for a specified file
     */
    public int[] GetChunkList(String filename) {
        return null;
    }

    /*
     * Description:
     *   Adds a file to the list of files this peer is offering
     */
    public void AddFileToList(String filename) {

    }

    /*
     * Description:
     *   Adds (or updates) the chunk list for the given filename.
     *   Indices into chunkList are based on lookup of the filename in fileList
     */
    public void AddChunkToList(String filename, int[] chunkList) {
        
    }
}
