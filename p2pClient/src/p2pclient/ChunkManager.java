package p2pclient;

import java.io.*;

/**
 * This class is used to manage the chunks that make up a file. It acts as the
 * gateway between the File and the GUI, the Receiving Client, and the Serving
 * Client classes.
 *
 * @author Matt
 */
public class ChunkManager {
    /** Filename this manager is for */
    public String filename;
    /** Has the download of this file started? */
    public boolean downloadStarted;
    /** Component chunks of this file */
    FileChunk[] chunkList;
    /** Location to write the file to once it has been completely received */
    public String completeFilePath;

    /**
     * The constructor for ChunkManager takes in the filename this ChunkManager
     * will be managing.  If the file exists on disc as a complete file, the
     * ChunkManager will import all its information from the complete file.
     * If a partially complete file is found on disc, the ChunkManager will
     * import all available information from the file and the rest of the
     * information from the torrent file.  If no file exists on disc, the
     * ChunkManager will create a new TrackerRequest object to get the torrent
     * from the tracker and create the chunkList based on the torrent.
     *
     * @param filename Filename this manager is for
     * @param completedFilePath Location to write the file to once it has been completely received
     */
    ChunkManager(String filename, String completedFilePath) {
        this.filename = filename;
        this.downloadStarted = false;
        this.completeFilePath = completedFilePath;
    }

    /**
     * Constructor to be used for a file to be downloaded after the torrent for
     * that file is received from the tracker.
     * 
     * @param torrent Torrent representing the file this manager is for
     * @param completedFilePath Location to write the file to once it has been completely received
     */
    ChunkManager(Torrent torrent, String completedFilePath) {
        this.filename = torrent.filename;
        this.downloadStarted = false;
        this.completeFilePath = completedFilePath;
        this.chunkList = new FileChunk[torrent.numChunks];
        int chunkNum;
        byte[] hash = new byte[ChunkInfo.HASH_SIZE];

        for (int i = 0; i < torrent.numChunks; i++) {
            chunkNum = torrent.chunks[i].chunkNumber;
            System.arraycopy(torrent.chunks[i].hash, 0, hash, 0, hash.length);
            this.chunkList[chunkNum] = new FileChunk(chunkNum, hash, 0, null);
        }
    }

    /**
     * Determines if this manager is for the given file
     *
     * @param filename Filename to check against
     * @return The chunk manager if it is a match, null otherwise
     */
    public synchronized ChunkManager FindChunkManager(String filename) {
        if ( this.filename.equals(filename) ) {
            return this;
        }
        return null;
    }

    /**
     * Obtain the list of chunks known to the manager
     *
     * @return The list of chunks
     */
    public synchronized ChunkInfo[] GetChunkInfo() {
        ChunkInfo[] chunkInfoList = new ChunkInfo[chunkList.length];
        for ( int i = 0; i < chunkList.length; i++ ) {
            chunkInfoList[i] = chunkList[i].chunkInfo;
        }
        return chunkInfoList;
    }

    /**
     * Obtain the list of chunks that have not yet been received by the manager
     *
     * @return The list of chunks
     */
    public synchronized ChunkInfo[] NeededChunks() {
        return GetChunkList(0);
    }

    /**
     * Obtain the list of chunks that are currently being downloaded by the manager
     *
     * @return The list of chunks
     */
    public synchronized ChunkInfo[] DownloadingChunks() {
        return GetChunkList(1);
    }

    /**
     * Obtain the list of chunks that have been completely received by the manager
     *
     * @return The list of chunks
     */
    public synchronized ChunkInfo[] AvailableChunks() {
        return GetChunkList(2);
    }

    /**
     * Obtain the list of chunks matching the given status.
     *
     * @note Helper function for NeededChunks(), DownloadingChunks(), and AvailableChunks()
     *
     * @param status Particular chunk status to match against
     *
     * @return The list of chunks
     */
    private synchronized ChunkInfo[] GetChunkList(int status) {
        ChunkInfo[] chunks = new ChunkInfo[0];

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

    /**
     * Updates a particular chunk
     *
     * @param chunkNumber Chunk number to update
     * @param chunkData Chunk data
     * @param receivedFrom Peer the chunk was received from
     */
    public synchronized void UpdateChunk(int chunkNumber, byte[] chunkData, Peer receivedFrom) {
        if ( chunkNumber < chunkList.length ) {
            chunkList[chunkNumber].UpdateChunk(chunkData, receivedFrom);
            chunkList[chunkNumber].chunk = chunkData.clone();
            //System.arraycopy(chunkData, 0, chunkList[chunkNumber].chunk, 0, chunkData.length);
        }

        // figure out if file is completed and write it to disk
        int completedCount = 0;
        int totalSize = 0;
        if (this.chunkList != null) {
            for (FileChunk fileChunk : this.chunkList) {
                if (fileChunk.chunkInfo != null && fileChunk.chunkInfo.status == 2 && fileChunk.chunk != null && fileChunk.chunk.length != 0) {
                    completedCount++;
                    totalSize += fileChunk.chunk.length;
                }
            }

            if (completedCount == this.chunkList.length) {
                byte[] fileData = new byte[totalSize];
                for (FileChunk fileChunk : this.chunkList) {
                    System.arraycopy(fileChunk.chunk, 0, fileData, fileChunk.chunkInfo.chunkNumber * Torrent.CHUNK_SIZE, fileChunk.chunk.length);
                }

                Util.WriteFileData(this.completeFilePath, this.filename, fileData);
            }
        }
    }

    /**
     * Set the status of a given chunk
     *
     * @param chunkNumber Chunk number to update
     * @param status New status
     */
    public synchronized void UpdateChunkStatus(int chunkNumber, int status) {
        chunkList[chunkNumber].chunkInfo.status = status;
    }

    /**
     * Obtain the chunk data for a given chunk
     *
     * @param chunkNumber Chunk number to obtain
     *
     * @return Byte array representing the chunk data
     */
    public synchronized byte[] GetChunkData(int chunkNumber) {
        if ( chunkNumber < chunkList.length ) {
           return chunkList[chunkNumber].chunk;
        }
        return null;
    }
}
