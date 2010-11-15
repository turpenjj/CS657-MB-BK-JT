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

    Peer(InetAddress ip, int port) {
        clientIp = ip;
        listeningPort = port;
        creditForThem = 0;
        creditForUs = 0;
        fileList = null;
        chunkList = null;
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
}
