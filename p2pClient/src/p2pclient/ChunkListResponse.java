package p2pclient;

/**
 * This class defines the chunk list response message sent from one peer to
 * another in response to a chunk list request message and provides methods for
 * creating such responses and interpreting them. The structure of the message
 * payload on the wire is:
 *
 * char[] filename; // null-terminated filename
 * uint32_t[] chunkNumbers; // list of chunk numbers this peer has available
 *
 * @author Matt
 */
public class ChunkListResponse {
    /** Filename this response is for */
    String filename;
    /** List of chunk numbers in the response */
    int[] chunkList;

    /**
     * Creates a new chunk list response object. Expected to be called by the
     * peer receiving the request, followed by a call to ImportMessagePayload().
     */
    ChunkListResponse() {}

    /**
     * Creates a new chunk list response object. Expected to be called by the
     * peer making the request, followed by a call to ExportMessagePayload().
     *
     * @param request Filename being queried for
     * @param port Port on which the response to this query should be sent
     */
    ChunkListResponse(String requestedFile, ChunkInfo[] chunkInfo) {
        filename = requestedFile;
        if ( chunkInfo == null ) {
            chunkList = null;
        } else {
            chunkList = new int[chunkInfo.length];
            for ( int i = 0; chunkInfo != null && i < chunkInfo.length; i++ ) {
                chunkList[i] = chunkInfo[i].chunkNumber;
            }
        }
    }

    /**
     * Form a response message
     *
     * @return Byte array representing the response message as it should appear on the wire
     */
    public byte[] ExportMessagePayload() {
        //length of filename plus null spacer, plus length of chunk list
        int chunkListLength;
        if ( chunkList == null ) {
            chunkListLength = 0;
        } else {
            chunkListLength = chunkList.length * 4;
        }
        int requestLength = filename.length() + 1 + chunkListLength;
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = Util.InsertNullTerminatedString(requestInBytes, 0, filename);
        indexIntoByteArray = Util.IntArrayToByteArray(requestInBytes, indexIntoByteArray, chunkList);

        return requestInBytes;
    }

    /**
     * Interpret a response message from the on-wire representation. Contents of
     * response then accessed via the public data members.
     *
     * @param data Byte array representing the response message received on the wire
     */
    public void ImportMessagePayload(byte[] data) {
        int indexIntoByteArray[] = new int[1];
        filename = Util.ExtractNullTerminatedString(data, 0, indexIntoByteArray);

        int chunkListLength = data.length - indexIntoByteArray[0];
        byte[] rawChunkList = new byte[chunkListLength];
        System.arraycopy(data, indexIntoByteArray[0], rawChunkList, 0, chunkListLength);
        chunkList = Util.ByteArrayToIntArray(rawChunkList);
    }
}
