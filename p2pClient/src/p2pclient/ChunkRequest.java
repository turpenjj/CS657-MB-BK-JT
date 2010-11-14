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
    Peer peer;

    /*
     * Description:
     *   The constructor for the client-side of the request
     */
    ChunkRequest(Peer servingPeer, String request, int number) {
        peer = servingPeer;
        filename = request;
        chunkNumber = number;
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

    /*
     * Description:
     *   Sends the chunk list request to the peer the request is destined for
     *
     * Returns:
     *   True if packet was sent
     *   False if an error occurred
     */
    public boolean Send() {
        return SendPacket(peer, this.ExportMessagePayload());
    }

}
