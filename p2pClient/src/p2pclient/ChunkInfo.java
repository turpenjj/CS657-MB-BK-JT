package p2pclient;

/**
 * Contains metadata about a chunk (piece of a file). The metadata includes a
 * hash for data integrity checks at the peers and the chunk number.
 *
 * Additional fields for peers to use in tracking the receipt of chunks are the
 * status and peer from which the chunk was received from.
 *
 * @note Does not contain the actual data. @see FileChunk
 *
 * @author Matt
 */
public class ChunkInfo {
    /** Offset into the data (chunk, not byte) this belongs at (zero-based) */
    public int chunkNumber;
    /** Size of the hash, in bytes */
    public static int HASH_SIZE = 20;
    /** Calculated hash (SHA1) for the chunk */
    byte[] hash = new byte[ChunkInfo.HASH_SIZE];
    /** Current status of this chunk (0 = missing, 1 = downloading, 2 = available) @todo Make an enum for these values */
    public volatile int status;
    /** Peer from which this chunk was received */
    public volatile Peer receivedFrom;

    /**
     * Creates a new ChunkInfo object
     * 
     * @param chNumber Chunk number for the created chunk
     * @param chunkHash Hash for the created chunk
     * @param stat Status for the created chunk
     */
    ChunkInfo(int chNumber, byte[]chunkHash, int stat) {
        chunkNumber = chNumber;
        System.arraycopy(chunkHash, 0, hash, 0, chunkHash.length);
        status = stat;
    }

    /**
     * Returns a string summary of relevant info about the chunk. Useful for
     * including in debug messages.
     *
     * @return String representation of the chunk
     */
    public String toString() {
        return Util.ConvertToHex(this.hash);
    }
}
