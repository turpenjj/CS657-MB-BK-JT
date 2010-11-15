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


    /*
     * Description:
     *   Gets a byte array representation of the request
     *
     * Returns:
     *   the chunk list request as a byte array
     */
    public byte[] ExportMessagePayload() {
        //length of filename plus null spacer, plus length of chunk list
        int chunkListLength;
        if ( chunkList == null ) {
            chunkListLength = 0;
        } else {
            chunkListLength = chunkList.length * 4;
        }
        int requestLength = filename.length() + 1 + chunkListLength * 4;
        byte[] requestInBytes = new byte[requestLength];
        int indexIntoByteArray = InsertNullTerminatedString(requestInBytes, 0, filename);
        indexIntoByteArray = IntArrayToByteArray(requestInBytes, indexIntoByteArray, chunkList);

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {
        int indexIntoByteArray[] = new int[1];
        filename = ExtractNullTerminatedString(data, 0, indexIntoByteArray);

        int chunkListLength = data.length - indexIntoByteArray[0];
        byte[] rawChunkList = new byte[chunkListLength];
        System.arraycopy(data, indexIntoByteArray[0], rawChunkList, 0, chunkListLength);
        chunkList = ByteArrayToIntArray(rawChunkList);
    }
}
