package p2pclient;

/**
 * This class defines the structure of the chunk list request message sent
 * between peers and provides methods for creating and interpreting such messages.
 * The structure of the message payload on the wire is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t listeningPort; // port to send the response to
 *
 * @author Matt
 */
public class ChunkListRequest {
    /** Port on which the response to this query should be sent */
    public int receivingPort;
    /** Filename being queried for */
    public String filename;

    /**
     * Creates a new chunk list request object. Expected to be called by the
     * peer making the request, followed by a call to ExportMessagePayload().
     *
     * @param request Filename being queried for
     * @param port Port on which the response to this query should be sent
     */
    ChunkListRequest(String request, int port) {
        receivingPort = port;
        filename = request;
    }

    /**
     * Creates a new chunk list request object. Expected to be called by the
     * peer receiving the request, followed by a call to ImportMessagePayload().
     */
    ChunkListRequest() {}

    /**
     * Form a request message for this request
     *
     * @return Byte array representing the request message as it should appear on the wire
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 5; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        Util.IntToByteArray(requestInBytes, 0, receivingPort);
        Util.InsertNullTerminatedString(requestInBytes, 4, filename);

        return requestInBytes;
    }

    /**
     * Interpret a request message from the on-wire representation. Contents of
     * the request then accessed via the public data members.
     *
     * @param data Byte array representing the request message received on the wire
     */
    public void ImportMessagePayload(byte[] data) {
        receivingPort = Util.ByteArrayToInt(data, 0);
        filename = Util.ExtractNullTerminatedString(data, 4, null);
    }
}
