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
    int receivingPort;
    String filename;

    ChunkListRequest(int port, String request) {
        receivingPort = port;
        filename = request;
    }

    public byte[] GetBytes() {
        int requestLength = filename.length() + 4; //filename + receiving port
        byte[] requestInBytes = new byte[requestLength];

        System.arraycopy(IntToByteArray(receivingPort), 0, requestInBytes, 0, 4);
        System.arraycopy(filename.getBytes(), 0, requestInBytes, 4, filename.getBytes().length);

        return requestInBytes;
    }

    public int GetSize() {
        return filename.length() + 4;
    }
}
