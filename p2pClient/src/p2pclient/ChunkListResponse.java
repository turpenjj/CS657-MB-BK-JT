/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkListResponse extends Util {
    String filename;
    int[] chunkList;

    /*
     * Description:
     *   The constructor for the client side of the response
     */
    ChunkListResponse() {}

    /*
     * Description:
     *   The constructor for the server side of the response
     */
    ChunkListResponse(String requestedFile, int[] chunkNumbers) {
        filename = requestedFile;
        chunkList = chunkNumbers;
    }


    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk list request as a byte array
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 4; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

//        System.arraycopy(IntToByteArray(peer.listeningPort), 0, requestInBytes, 0, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 4, filename.getBytes().length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {

    }

}
