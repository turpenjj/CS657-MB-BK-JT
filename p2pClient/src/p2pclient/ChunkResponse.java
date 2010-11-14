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
    PacketHeader packetHeader;
    Peer peer;
    FileChunk chunk;
    String filename;

    /*
     * Description:
     *   The constructor for the client-end of the response
     */
    ChunkResponse() {}

    /*
     * Description:
     *   The constructor for the server-end of the response
     */
    ChunkResponse(String requestedFilename, int chunkNumber, byte[] chunkData) {
        filename = requestedFilename;
        chunk.chunkInfo.chunkNumber = chunkNumber;
        System.arraycopy(chunkData, 0, chunk.chunk, 0, chunkData.length);
    }

    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk as a byte array
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 4; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(IntToByteArray(peer.listeningPort), 0, requestInBytes, 0, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 4, filename.getBytes().length);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {

    }

    /*
     * Description:
     *   Sends the chunk to the peer who requested it
     *s
     * Returns:
     *   True if packet was sent
     *   False if an error occurred
     */
    public void Send() {
        SendPacket(peer, this.ExportMessagePayload());
    }
}
