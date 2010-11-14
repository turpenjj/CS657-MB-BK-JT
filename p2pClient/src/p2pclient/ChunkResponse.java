/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkResponse extends Util {
    public String filename;
    public int chunkNumber;
    byte[] chunkData;

    /*
     * Description:
     *   The constructor for the client-end of the response
     */
    ChunkResponse() {}

    /*
     * Description:
     *   The constructor for the server-end of the response
     */
    ChunkResponse(String requestedFilename, int setChunkNumber, byte[] setChunkData) {
        chunkData = new byte[setChunkData.length];
        filename = requestedFilename;
        chunkNumber = setChunkNumber;
        System.arraycopy(setChunkData, 0, chunkData, 0, chunkData.length);
    }

    /*
     * Description:
     *   Gets a byte array representation of the response
     *
     * Returns:
     *   the chunk as a byte array
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length(); //filename
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(filename.getBytes(), 0, requestInBytes, 0, filename.getBytes().length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {

    }
}
