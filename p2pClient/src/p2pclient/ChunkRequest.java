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
        int requestLength = 0; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {

    }

}
