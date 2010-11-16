/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkRequest {
    String filename;
    int chunkNumber;
    int listeningPort;

    /*
     * Description:
     *   The constructor for the client-side of the request
     */
    ChunkRequest(String request, int number, int port) {
        filename = request;
        chunkNumber = number;
        listeningPort = port;
    }

    /*
     * Description:
     *   The constructor for the server-side of the request
     */
    ChunkRequest() {}

    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk request as a byte array
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 9; // 1 for null-termination of string, 4  for chunk number, 4 for port
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = 0;

        indexIntoByteArray = Util.IntToByteArray(requestInBytes, indexIntoByteArray, chunkNumber);
        indexIntoByteArray = Util.IntToByteArray(requestInBytes, indexIntoByteArray, listeningPort);
        Util.InsertNullTerminatedString(requestInBytes, indexIntoByteArray, filename);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {
        int currentIndex = 0;
        chunkNumber = Util.ByteArrayToInt(data, currentIndex);
        currentIndex += 4;
        listeningPort = Util.ByteArrayToInt(data, currentIndex);
        currentIndex += 4;
        filename = Util.ExtractNullTerminatedString(data, currentIndex, null);
    }

}
