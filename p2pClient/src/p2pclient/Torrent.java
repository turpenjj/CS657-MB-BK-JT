package p2pclient;

import java.io.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Defines a data class for a torrent (the file and details about its component
 * chunks)
 *
 * @author Jeremy
 */
public class Torrent {
    public String filename;
    public int filesize;
    public int numChunks;
    public ChunkInfo[] chunks;
    public static int CHUNK_SIZE = 100;
    public long timestamp; // timestamp at which this torrent was created

    /**
     * Tracker constructor. Called with info from TRACKER_REGISTER_TORRENT message.
     */
    Torrent(String filename, int filesize, ChunkInfo[] chunkInfoList) {
        this.filename = filename;
        this.filesize = filesize;
        this.chunks = chunkInfoList;
        this.numChunks = chunkInfoList.length;
        this.timestamp = Util.GetCurrentTime();
    }

    /**
     * Peer constructor. Called if we need to create a TRACKER_REGISTER_TORRENT
     * for this file.
     */
    Torrent(String path, String filename) throws Exception {
        File file = new File(path + filename);
        FileInputStream fileInputStream = new FileInputStream(file);
        long totalLength = file.length();
        byte[] fileData = new byte[(int)totalLength];
        byte[] chunkData = new byte[Torrent.CHUNK_SIZE];
        int i;
        int bytesRead;
        int currentIndex;
        int currentChunkSize;
        ChunkInfo chunkInfo;
        byte[] sum;

        this.filename = filename;
        this.filesize = (int)totalLength;

        currentIndex = 0;
        bytesRead = 1;
        while (currentIndex < fileData.length && bytesRead > 0) {
            bytesRead = fileInputStream.read(fileData, currentIndex, fileData.length - currentIndex);
            if (bytesRead > 0) {
                currentIndex += bytesRead;
            }
        }
        
        this.numChunks = (int)(totalLength / Torrent.CHUNK_SIZE);
        if (this.numChunks * Torrent.CHUNK_SIZE < totalLength) {
            this.numChunks++;
        }

        for (i = 0; i < this.numChunks; i++) {
            currentIndex = i * Torrent.CHUNK_SIZE;

            if (currentIndex + Torrent.CHUNK_SIZE > totalLength) {
                currentChunkSize = ((int)totalLength)- currentIndex;
            } else {
                currentChunkSize = Torrent.CHUNK_SIZE;
            }

            System.arraycopy(fileData, currentIndex, chunkData, 0, currentChunkSize);
            sum = SHA1(chunkData);
            chunkInfo = new ChunkInfo(i, sum, 2);
            this.chunks = AddToChunkInfoList(this.chunks, chunkInfo);
        }
    }

    public static ChunkInfo[] AddToChunkInfoList(ChunkInfo[] list, ChunkInfo chunk) {
        ChunkInfo[] temp;

        if (list == null || list[0] == null) {
            temp = new ChunkInfo[1];
            temp[0] = chunk;

            return temp;
        }

        temp = new ChunkInfo[list.length + 1];

        temp[0] = chunk;
        System.arraycopy(list, 0, temp, 1, list.length);

        return temp;
    }

    private byte[] SHA1 (byte[] toHash) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");

            md.update(toHash, 0, toHash.length);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Util.DebugPrint(DbgSub.TORRENT, "Trouble, no SHA1 algorithm");
            return null;
        }
    }

    public String toString() {
        return "Filename = '" + this.filename + "'; File size = " + this.filesize + "; Number of Chunks = " + this.numChunks;

    }
}
