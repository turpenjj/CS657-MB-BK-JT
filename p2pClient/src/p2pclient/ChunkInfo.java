/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkInfo {
    int chunkNumber;
    byte[] hash = new byte[20];
    int status; //0 = missing, 1 = downloading, 2 = available
    Peer receivedFrom;

    ChunkInfo(int chNumber, byte[]chunkHash, int stat) {
        chunkNumber = chNumber;
        System.arraycopy(chunkHash, 0, hash, 0, chunkHash.length);
        status = stat;
    }
}