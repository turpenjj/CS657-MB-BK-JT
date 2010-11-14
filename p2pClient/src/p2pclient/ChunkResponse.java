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

        indexIntoByteArray += InsertNullTerminatedString(requestInBytes, 0, filename);
        indexIntoByteArray += IntToByteArray(requestInBytes, indexIntoByteArray, chunkNumber);
        System.arraycopy(chunkData, 0, requestInBytes, indexIntoByteArray, chunkData.length);
//        System.arraycopy(filename.getBytes(), 0, requestInBytes, 0, filename.getBytes().length);
//        requestInBytes[filename.length()] = 0x00;
//        System.arraycopy(IntToByteArray(chunkNumber), 0, requestInBytes, filename.length() + 1, 4);
//        System.arraycopy(chunkData, 0, requestInBytes, filename.length() + 5, chunkData.length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {
        int[] indexIntoByteArray = new int[1];
        filename = ExtractNullTerminatedString(data, 0, indexIntoByteArray);
        chunkNumber = ByteArrayToInt(data, indexIntoByteArray[0]);
        indexIntoByteArray[0] += 4;
        chunkData = new byte[data.length - indexIntoByteArray[0]];
        System.arraycopy(data, indexIntoByteArray[0], chunkData, 0, chunkData.length);

//        int StringLength = 0;
//        while ( data[StringLength] != 0x00 ) {
//            StringLength++;
//        }
//        byte[] stringInBytes = new byte[StringLength];
//        System.arraycopy(data, 0, stringInBytes, 0, StringLength);
//        filename = new String(stringInBytes);
//
//        byte[] rawChunkNumber = new byte[4];
//        System.arraycopy(data, StringLength + 1, rawChunkNumber, 0, 4);
//        chunkNumber = ByteArrayToInt(rawChunkNumber);
//        chunkData = new byte[data.length - StringLength - 5];
//        System.arraycopy(data, StringLength + 5, chunkData, 0, data.length - StringLength - 5);

    }
}
