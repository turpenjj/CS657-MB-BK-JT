/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 * Description:
 *  This class is used to manage the chunks that make up a file.  It acts as the gateway between the File and the GUI, the Receiving
 *  Client, and the Serving Client classes
 *
 * @author Matt
 */
public class ChunkManager {
    public String filename;
//    Peer[] peerList;
    FileChunk[] chunkList;

    /*
     * Description:
     *   The constructr for ChunkManager takes in the filename this ChunkManager
     *   will be managing.  If the file exists on disc as a complete file, the
     *   ChunkManager will import all its information from the complete file.
     *   If a partially complete file is found on disc, the ChunkManager will
     *   import all available information from the file and the rest of the
     *   information from the torrent file.  If no file exists on disc, the
     *   ChunkManager will create a new TrackerRequest object to get the torrent
     *   from the tracker and create the chunkList based on the torrent.
     */
    ChunkManager(String newFile) {
        filename = newFile;
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo that make up this file
     */
    public ChunkInfo[] GetChunkInfo() {
        return null;
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo for all chunks still needed to complete
     *   the file
     */
    public ChunkInfo[] NeededChunks() {
        return null;
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo for all chunks that this host already
     *   has for the file
     */
    public ChunkInfo[] AvailableChunks() {
        return null;
    }

    /*
     * Description:
     *   Updates the chunk indicated by chunkNumber with the data in chunkData
     *   If the hash of chunkData matches what is expected, the chunk is written
     *   to disc.  Otherwise, false is returned.
     */
    public boolean UpdateChunk(int chunkNumber, byte[] chunkData, Peer receivedFrom) {
        return false;
    }

    /*
     * Description:
     *   Returns the data associated with the given chunk
     */
    public byte[] GetChunkData(int chunkNumber) {
        return null;
    }
}
