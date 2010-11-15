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
        //filename, null byte, chunk number, chunk data
        int requestLength = filename.length() + 5 + chunkData.length;
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = 0;

        indexIntoByteArray = InsertNullTerminatedString(requestInBytes, 0, filename);
        indexIntoByteArray = IntToByteArray(requestInBytes, indexIntoByteArray, chunkNumber);
        System.arraycopy(chunkData, 0, requestInBytes, indexIntoByteArray, chunkData.length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {
        int[] indexIntoByteArray = {0};
        filename = ExtractNullTerminatedString(data, 0, indexIntoByteArray);
        chunkNumber = ByteArrayToInt(data, indexIntoByteArray[0]);
        indexIntoByteArray[0] += 4;
        chunkData = new byte[data.length - indexIntoByteArray[0]];
        System.arraycopy(data, indexIntoByteArray[0], chunkData, 0, chunkData.length);
    }
}
