/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class PacketHeader extends Util {
    int sessionID;
    PacketType packetType;
    int totalSize; //NOT including the packet header(s)
    int offset;

    PacketHeader(int sID, PacketType pT, int ts, int off) {
        sessionID = sID;
        packetType = pT;
        totalSize = ts;
        offset = off;
    }

    public byte[] GetBytes() {
        byte[] byteHeader = new byte[16];
        System.arraycopy(IntToByteArray(sessionID), 0, byteHeader, 0, 4);
        System.arraycopy(IntToByteArray(packetType.ordinal()), 0, byteHeader, 4, 4);
        System.arraycopy(IntToByteArray(totalSize), 0, byteHeader, 8, 4);
        System.arraycopy(IntToByteArray(offset), 0, byteHeader, 12, 4);

        return byteHeader;
    }
}
