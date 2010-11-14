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
    ChunkListResponse(String requestedFile, int[] chunkNumbers) {
        chunkList = new int[chunkNumbers.length];
        filename = requestedFile;
        System.arraycopy(chunkNumbers, 0, chunkList, 0, chunkNumbers.length);
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
        int requestLength = filename.length() + 1 + chunkList.length * 4;
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(filename.getBytes(), 0, requestInBytes, 0, filename.getBytes().length);
        requestInBytes[filename.length()] = 0x00;
        for ( int i = 0; i < chunkList.length; i++ ) {
            System.arraycopy(IntToByteArray(chunkList[i]), 0, requestInBytes, filename.length() + 1 + i*4, 4);
        }

        return requestInBytes;
    }

    /*
     * Description:
     *   Imports the payload portion of the response
     */
    public void ImportMessagePayload(byte[] data) {
        int StringLength = 0;
        while ( data[StringLength] != 0x00 ) {
            StringLength++;
        }
        byte[] stringInBytes = new byte[StringLength];
        System.arraycopy(data, 0, stringInBytes, 0, StringLength);
        filename = new String(stringInBytes);
        int chunkListLength = data.length - StringLength - 1;
        byte[] rawChunkList = new byte[chunkListLength];
        System.arraycopy(data, StringLength + 1, rawChunkList, 0, chunkListLength);
        chunkList = ByteArrayToIntArray(rawChunkList);
    }
}
