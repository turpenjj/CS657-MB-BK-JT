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
    public int chunkNumber;
    public static int HASH_SIZE = 20;
    byte[] hash = new byte[ChunkInfo.HASH_SIZE];
    public volatile int status; //0 = missing, 1 = downloading, 2 = available
    public volatile Peer receivedFrom;


    ChunkInfo(int chNumber, byte[]chunkHash, int stat) {
        chunkNumber = chNumber;
        System.arraycopy(chunkHash, 0, hash, 0, chunkHash.length);
        status = stat;
    }

    public String toString() {
        return Util.ConvertToHex(this.hash);
    }
}
