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
    PacketHeader packetHeader;
    String filename;
    int[] chunkNumber;
    Peer peer;

    ChunkListResponse(Peer requestingPeer, String request) {
        peer = requestingPeer;
        filename = request;
    }

    /*
     * Description:
     *   Determines the available chunks for the requested file and returns the chunk List
     *
     * Returns:
     *   int[] containing all chunks we have available to share
     *   null if we don't have any chunks
     */
    public int[] GetChunkList() {
        int[] chunkList = null;

        return chunkList;
    }

    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk list request as a byte array
     */
    public byte[] GetBytes() {
        int requestLength = filename.length() + 4; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(IntToByteArray(peer.listeningPort), 0, requestInBytes, 0, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 4, filename.getBytes().length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Sends the chunk list to the peer who requested it
     *s
     * Returns:
     *   True if packet was sent
     *   False if an error occurred
     */
    public void Send() {
        SendPacket(peer, this.GetBytes());
    }

}
