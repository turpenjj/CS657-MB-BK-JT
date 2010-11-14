/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkRequest extends Util {
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
        int requestLength = filename.length() + 4 + 4; // 4  for chunk number, 4 for port
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(IntToByteArray(chunkNumber), 0, requestInBytes, 0, 4);
        System.arraycopy(IntToByteArray(listeningPort), 0, requestInBytes, 4, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 8, filename.length());

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {
        byte[] tempByteArray = new byte[4];
        System.arraycopy(data, 0, tempByteArray, 0, 4);
        chunkNumber = ByteArrayToInt(tempByteArray);
        System.arraycopy(data, 4, tempByteArray, 0, 4);
        listeningPort = ByteArrayToInt(tempByteArray);
        byte[] stringInBytes = new byte[data.length - 8];
        System.arraycopy(data, 8, stringInBytes, 0, data.length - 8);
        filename = new String(stringInBytes);
    }

}
