package p2pclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains a chunk (piece of a file). Includes the metadata and the actual chunk
 * data.
 *
 * @author Matt
 */
public class FileChunk {
    /** Metadata about the chunk */
    public volatile ChunkInfo chunkInfo;
    /** Actual chunk data */
    byte[] chunk;

    /**
     * Creates a new chunk
     *
     * @param chunkNumber Chunk number
     * @param chunkHash Hash of the chunk
     * @param status Starting status
     * @param chunk Chunk data
     */
    FileChunk(int chunkNumber, byte[] chunkHash, int status, byte[] chunk) {
        chunkInfo = new ChunkInfo(chunkNumber, chunkHash, status);
        this.chunk = chunk;
    }

    /**
     * Associates the chunk with new data. Updates the peer's credit level.
     *
     * @note If the chunk data hash does not match the expected hash value, the new data is not used
     *
     * @param newData New chunk data
     * @param receivedFrom Peer from which the data was received
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

    /**
     * Sets the data for this chunk and updates the hash
     *
     * @param data New chunk data
     * @param length Length of the data to use
     */
    public void SetChunk(byte[] data, int length) {
        chunk = new byte[length];
        System.arraycopy(data, 0, chunk, 0, length);
        CalcSha1Hash();
    }

    /**
     * Obtains the hash for this chunk
     * @return Array of bytes representing the hash
     */
    public byte[] GetHash() {
        return chunkInfo.hash;
    }

    /**
     * Obtains the hash for this chunk
     * @return String representation of the hash
     */
    public String GetHashString() {
        return Util.ConvertToHex(chunkInfo.hash);
    }

    /**
     * Verifies the given data matches the expected hash value for this chunk
     * @param newChunk Chunk data
     * @return true if it does, false otherwise
     */
    private boolean VerifyHash(byte[] newChunk) {
        byte[] newChunkHash = SHA1(newChunk);
        if ( GetHashString().equals(Util.ConvertToHex(newChunkHash)) ) {
            return true;
        }
        return false;
    }

    /**
     * Recalculates this chunks hash
     */
    private void CalcSha1Hash() {
        chunkInfo.hash = SHA1(chunk);
    }

    /**
     * Calculates the SHA1 hash for the given data
     *
     * @param toHash Data to hash
     * @return Array of bytes representing the hash
     */
    private byte[] SHA1 (byte[] toHash) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");

            md.update(toHash, 0, toHash.length);
            chunkInfo.hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            Util.DebugPrint(DbgSub.FILE_CHUNK, "Trouble, no SHA1 algorithm");
        }
        return chunkInfo.hash;
    }
}
