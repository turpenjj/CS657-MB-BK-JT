package p2pclient;

/**
 * This class defines the chunk response message sent from one peer to another
 * in response to a chunk request message and provides methods for creating
 * such responses and interpreting them. The structure of the message payload
 * on the wire is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t chunkNumbers; // chunk number for this response
 * byte[] data; // chunk data
 *
 * @author Matt
 */
public class ChunkResponse {
    /** Filename being requested */
    public String filename;
    /** Chunk number for this response */
    public int chunkNumber;
    /** Chunk data */
    byte[] chunkData;

    /**
     * Creates a new chunk list response object. Expected to be called by the
     * peer receiving the request, followed by a call to ImportMessagePayload().
     */
    ChunkResponse() {}

    /**
     * Creates a new chunk list response object. Expected to be called by the
     * peer making the request, followed by a call to ExportMessagePayload().
     *
     * @param requestedFilename Filename being requested
     * @param setChunkNumber Chunk number for this response
     * @param setChunkData Chunk data
     */
    ChunkResponse(String requestedFilename, int setChunkNumber, byte[] setChunkData) {
        chunkData = new byte[setChunkData.length];
        filename = requestedFilename;
        chunkNumber = setChunkNumber;
        System.arraycopy(setChunkData, 0, chunkData, 0, chunkData.length);
    }

    /**
     * Form a response message
     *
     * @return Byte array representing the response message as it should appear on the wire
     */
    public byte[] ExportMessagePayload() {
        //filename, null byte, chunk number, chunk data
        int requestLength = filename.length() + 5 + chunkData.length;
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = 0;

        indexIntoByteArray = Util.InsertNullTerminatedString(requestInBytes, 0, filename);
        indexIntoByteArray = Util.IntToByteArray(requestInBytes, indexIntoByteArray, chunkNumber);
        System.arraycopy(chunkData, 0, requestInBytes, indexIntoByteArray, chunkData.length);

        return requestInBytes;
    }

    /**
     * Interpret a response message from the on-wire representation. Contents of
     * response then accessed via the public data members.
     *
     * @param data Byte array representing the response message received on the wire
     */
    public void ImportMessagePayload(byte[] data) {
        int[] indexIntoByteArray = {0};
        filename = Util.ExtractNullTerminatedString(data, 0, indexIntoByteArray);
        chunkNumber = Util.ByteArrayToInt(data, indexIntoByteArray[0]);
        indexIntoByteArray[0] += 4;
        chunkData = new byte[data.length - indexIntoByteArray[0]];
        System.arraycopy(data, indexIntoByteArray[0], chunkData, 0, chunkData.length);
    }
}
