/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkListRequest extends Util {
    public int receivingPort;
    public String filename;

    /*
     * Description:
     *   The constructor for the client side of the request
     */
    ChunkListRequest(String request, int port) {
        receivingPort = port;
        filename = request;
    }

    /*
     * Description:
     *   The constructor for the server side of the request
     */
    ChunkListRequest() {}

    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk list request as a byte array
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 5; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        IntToByteArray(requestInBytes, 0, receivingPort);
        InsertNullTerminatedString(requestInBytes, 4, filename);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {
        receivingPort = ByteArrayToInt(data, 0);
        filename = ExtractNullTerminatedString(data, 4, null);
    }

}
