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
    PacketHeader packetHeader;
    String filename;
    int chunkNumber;
    Peer peer;

    ChunkRequest(Peer servingPeer, String request, int number) {
        peer = servingPeer;
        filename = request;
        chunkNumber = number;
    }

    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk request as a byte array
     */
    public byte[] GetBytes() {
        int requestLength = 0; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        return requestInBytes;
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
        return SendPacket(peer, this.GetBytes());
    }

}
