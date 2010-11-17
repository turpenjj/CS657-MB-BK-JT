package p2pclient;

/**
 * This class defines the structure of the chunk request message sent between
 * peers and provides methods for creating and interpreting such messages.
 * The structure of the message payload on the wire is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t chunkNumber; // chunk number requested
 * uint32_t listeningPort; // port to send the response to
 *
 * @author Matt
 */
public class ChunkRequest {
    /** Filename being queried for */
    String filename;
    /** Chunk number being requested */
    int chunkNumber;
    /** Port on which the response to this query should be sent */
    int listeningPort;

    /**
     * Creates a new chunk request object. Expected to be called by the peer
     * making the request, followed by a call to ExportMessagePayload().
     *
     * @param request Filename being queried for
     * @param number Chunk number being requested
     * @param port Port on which the response to this query should be sent
     */
    ChunkRequest(String request, int number, int port) {
        filename = request;
        chunkNumber = number;
        listeningPort = port;
    }

    /**
     * Creates a new chunk request object. Expected to be called by the peer
     * receiving the request, followed by a call to ImportMessagePayload().
     */
    ChunkRequest() {}

    /**
     * Interpret a request message from the on-wire representation. Contents of
     * the request then accessed via the public data members.
     *
     * @return Byte array representing the request message received on the wire
     */
    public byte[] ExportMessagePayload() {
        int requestLength = filename.length() + 9; // 1 for null-termination of string, 4  for chunk number, 4 for port
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = 0;

        indexIntoByteArray = Util.IntToByteArray(requestInBytes, indexIntoByteArray, chunkNumber);
        indexIntoByteArray = Util.IntToByteArray(requestInBytes, indexIntoByteArray, listeningPort);
        Util.InsertNullTerminatedString(requestInBytes, indexIntoByteArray, filename);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the request
     */
    public void ImportMessagePayload(byte[] data) {
        int currentIndex = 0;
        chunkNumber = Util.ByteArrayToInt(data, currentIndex);
        currentIndex += 4;
        listeningPort = Util.ByteArrayToInt(data, currentIndex);
        currentIndex += 4;
        filename = Util.ExtractNullTerminatedString(data, currentIndex, null);
    }

}
