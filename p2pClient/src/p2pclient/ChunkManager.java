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
    public boolean downloadStarted;
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
        downloadStarted = false;
    }

    public synchronized ChunkManager FindChunkManager(String filename) {
        if ( this.filename.equals(filename) ) {
            return this;
        }
        return null;
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo that make up this file
     */
    public synchronized ChunkInfo[] GetChunkInfo() {
        ChunkInfo[] chunkInfoList = new ChunkInfo[chunkList.length];
        for ( int i = 0; i < chunkList.length; i++ ) {
            chunkInfoList[i] = chunkList[i].chunkInfo;
        }
        return chunkInfoList;
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo for all chunks still needed to complete
     *   the file
     */
    public synchronized ChunkInfo[] NeededChunks() {
        return GetChunkList(0);
    }

    public synchronized ChunkInfo[] DownloadingChunks() {
        return GetChunkList(1);
    }

    /*
     * Description:
     *   Returns an array of ChunkInfo for all chunks that this host already
     *   has for the file
     */
    public synchronized ChunkInfo[] AvailableChunks() {
        return GetChunkList(2);
    }

    private synchronized ChunkInfo[] GetChunkList(int status) {
        ChunkInfo[] chunks = null;

        for ( int i = 0; i < chunkList.length; i++ ) {
            if ( chunkList[i].chunkInfo.status == status ) {
                if ( chunks == null ) {
                    chunks = new ChunkInfo[1];
                } else {
                    ChunkInfo[] tempList = new ChunkInfo[chunks.length + 1];
                    System.arraycopy(chunks, 0, tempList, 0, chunks.length);
                    chunks = tempList;
                }
                chunks[chunks.length-1] = chunkList[i].chunkInfo;
            }
        }
        return chunks;
    }

    /*
     * Description:
     *   Updates the chunk indicated by chunkNumber with the data in chunkData
     */
    public synchronized void UpdateChunk(int chunkNumber, byte[] chunkData, Peer receivedFrom) {
        if ( chunkNumber < chunkList.length ) {
            chunkList[chunkNumber].UpdateChunk(chunkData, receivedFrom);
        }
    }

    public synchronized void UpdateChunkStatus(int chunkNumber, int status) {
        chunkList[chunkNumber].chunkInfo.status = status;
    }

    /*
     * Description:
     *   Returns the data associated with the given chunk
     */
    public synchronized byte[] GetChunkData(int chunkNumber) {
        if ( chunkNumber < chunkList.length ) {
           return chunkList[chunkNumber].chunk;
        }
        return null;
    }
}
