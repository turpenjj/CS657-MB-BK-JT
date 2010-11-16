/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;
/**
 *
 * @author Matt
 */
public class Peer {
    public InetAddress clientIp;
    int listeningPort;
    public String[] fileList;
    int[][] chunkList; //[fileIndex][chunkIndex]
    public int creditForThem; //The credit associated with this peer
    public int creditForUs; //An approximation of the credit this peer has for us
    public int outstandingRequests; //The number of outstanding chunk requests for this peer
    long lastServiced; //timestamp of the last time we've serviced this peer
    public String[] filesWeSent;
    public int[][] chunksWeSent; //[fileIndex][]

    Peer(InetAddress ip, int port) {
        clientIp = ip;
        listeningPort = port;
        creditForThem = 0;
        creditForUs = 0;
        fileList = null;
        chunkList = null;
        outstandingRequests = 0;
        lastServiced = Util.GetCurrentTime();
    }

    /*
     * Description:
     *   Returns the list of chunks this peer claims to have for a specified file
     */
    public synchronized int[] GetChunkList(String filename) {
        if ( fileList != null ) {
            for (int i = 0; i < fileList.length; i++ ) {
                if ( fileList[i].equals(filename) ) {
                    return chunkList[i];
                }
            }
        }
        return null;
    }

    /*
     * Description:
     *   Adds a file to the list of files this peer is offering
     */
    public synchronized void AddFileToList(String filename) {
        if ( !HasFile(filename) ) {
            if ( fileList == null ) {
                fileList = new String[1];
                chunkList = new int[1][];
            } else {
                String[] tempList = new String[fileList.length + 1];
                int[][] tempChunkList = new int[chunkList.length + 1][];
                System.arraycopy(fileList, 0, tempList, 0, fileList.length);
                System.arraycopy(chunkList, 0, tempChunkList, 0, chunkList.length);
                fileList = tempList;
                chunkList = tempChunkList;
            }
            fileList[fileList.length - 1] = filename;
        }
    }
    
    public synchronized boolean HasFile(String filename) {
        if ( fileList != null ) {
            for (int i = 0; i < fileList.length; i++ ) {
                if ( fileList[i].equals(filename) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public synchronized boolean HasChunk(String filename, int chunkNumber) {
        int[] fileChunkList = GetChunkList(filename);
        if ( fileChunkList != null ) {
            for ( int i = 0; i < fileChunkList.length; i++ ) {
                if ( fileChunkList[i] == chunkNumber ) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /*
     * Gets the index into fileList[] (and chunkList[][]) for the given file
     * if the file doesn't exist in the list, -1 is returned
     */
    private synchronized int GetFileIndex(String filename) {
        if ( fileList != null ) {
            for (int i = 0; i < fileList.length; i++ ) {
                if ( fileList[i].equals(filename) ) {
                    return i;
                }
            }
        }
        return -1;
    }
    /*
     * Description:
     *   Updates (or adds) the chunk list for the given filename.
     *   Indices into chunkList are based on lookup of the filename in fileList
     */
    public synchronized void UpdateChunkList(String filename, int[] chunkList) {
        AddFileToList(filename);
        int fileIndex = GetFileIndex(filename);
        this.chunkList[fileIndex] = chunkList;
    }

    /*
     * Determines if we should request a chunk from this host.  If we don't have
     * much credit with them, we might not want to request a lot of things from
     * them.  This will help throttle our requests to a given peer.
     */
    public synchronized boolean ShouldRequest() {
        int THROTTLE = 3;
        if ( outstandingRequests > creditForUs + THROTTLE ) {
            return false;
        }
        return true;
    }

    /*
     * Updates the list of chunks we've sent to this peer
     * Useful for viewing by the GUI
     */
    public synchronized void SentChunk(String filename, int chunkNumber) {
        int fileIndex = GetFileIndexIntoSentList(filename);
        if ( fileIndex == -1 ) {
            filesWeSent = new String[1];
            filesWeSent[0] = filename;
            chunksWeSent = new int[1][];
            chunksWeSent[0] = new int[1];
            chunksWeSent[0][0] = chunkNumber;
        } else {
            int[] tempList = new int[chunksWeSent[fileIndex].length];
            tempList[0] = chunkNumber;
            System.arraycopy(chunksWeSent[fileIndex], 0, tempList, 1, chunksWeSent[fileIndex].length);
            chunksWeSent[fileIndex] = tempList;
        }
    }

    /*
     * Gets the index into fileList[] (and chunkList[][]) for the given file
     * if the file doesn't exist in the list, -1 is returned
     */
    private synchronized int GetFileIndexIntoSentList(String filename) {
        if ( filesWeSent != null ) {
            for (int i = 0; i < filesWeSent.length; i++ ) {
                if ( filesWeSent[i].equals(filename) ) {
                    return i;
                }
            }
        }
        return -1;
    }

    public String toString() {
        return this.clientIp + ":" + this.listeningPort;
    }
}
