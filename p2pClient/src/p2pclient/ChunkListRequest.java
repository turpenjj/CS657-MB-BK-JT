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
    Peer peer;

    /*
     * Description:
     *   The constructor for the client side of the request
     */
    ChunkListRequest(Peer servingPeer, int port, String request) {
        peer = servingPeer;
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
        int requestLength = filename.length() + 4; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(IntToByteArray(receivingPort), 0, requestInBytes, 0, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 4, filename.getBytes().length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {
        
    }

    public int GetSize() {
        return filename.length() + 4;
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
