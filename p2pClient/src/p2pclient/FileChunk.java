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
public class FileChunk {
    public volatile ChunkInfo chunkInfo;
    byte[] chunk;

    /*
     * Description:
     *   Passes the constructor through to the ChunkInfo Constructor
     */
    FileChunk(int chunkNumber, byte[] chunkHash, int status, byte[] chunk) {
        chunkInfo = new ChunkInfo(chunkNumber, chunkHash, status);
        this.chunk = chunk;
    }

    /*
     * Description:
     *   Updates the chunk with newData.
     *   If the hashof the new data doesn't match the expected hash, the new chunk
     *   is not updated
     */
    public void UpdateChunk(byte[] newData, Peer receivedFrom) {
        //if we have newData, we better have a peer we've received it from
        if ( newData != null ) {
            if ( VerifyHash(newData) ) {
                //Finished chunk means we're good to go.
                chunkInfo.status = 2;
                receivedFrom.creditForThem++;
            } else {
                //If we are updating the status, that means the peer has finished
                //sending the chunk... if the hash doesn't match, drop the chunk
                //and set the status back to missing
                chunkInfo.status = 0;
            }
        } else if ( receivedFrom != null ) {
            //If we are updating a chunk with a peer but no data, that means we've
            //started a new download for the chunk.
            chunkInfo.status = 1;
        } else {
            //If neither is set, the chunk download timed out, so set it back to missing
            chunkInfo.status = 0;
        }
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
        return Util.ConvertToHex(chunkInfo.hash);
    }

    private boolean VerifyHash(byte[] newChunk) {
        byte[] newChunkHash = SHA1(newChunk);
        if ( GetHashString().equals(Util.ConvertToHex(newChunkHash)) ) {
            return true;
        }
        return false;
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
