/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 *
 * @author Matt
 */
public class FileChunk extends Util {
    ChunkInfo chunkInfo;
    byte[] chunk;

    /*
     * Description:
     *   Passes the constructor through to the ChunkInfo Constructor
     */
    FileChunk(int chunkNumber, byte[] chunkHash, int status) {
        chunkInfo = new ChunkInfo(chunkNumber, chunkHash, status);
    }

    /*
     * Description:
     *   Updates the chunk with newData.
     *   Returns false if the hash of newData doesn't match what is expected
     */
    public boolean UpdateChunk(byte[] newData, Peer receivedFrom) {
        return false;
    }

    /*
     * Description:
     *   Returns the byte data for this chunk
     */
    public byte[] GetChunkData() {
        return null;
    }

    public void SetChunk(byte[] data, int length) {
        chunk = new byte[length];
        System.arraycopy(data, 0, chunk, 0, length);
        CalcSha1Hash();
    }

    public byte[] GetHash() {
        return chunkInfo.hash;
    }

    public String GetHashString() {
        return ConvertToHex(chunkInfo.hash);
    }

    private void CalcSha1Hash() {
        chunkInfo.hash = SHA1(chunk);
    }

    private byte[] SHA1 (byte[] toHash) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");

            md.update(toHash, 0, toHash.length);
            chunkInfo.hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Trouble, no SHA1 algorithm");
        }
        return chunkInfo.hash;
    }
}
