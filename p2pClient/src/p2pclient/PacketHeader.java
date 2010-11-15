/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class PacketHeader  {
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

    PacketHeader() {

    }

    public byte[] GetBytes() {
        byte[] byteHeader = new byte[16];
        System.arraycopy(Util.IntToByteArray(sessionID), 0, byteHeader, 0, 4);
        System.arraycopy(Util.IntToByteArray(packetType.ordinal()), 0, byteHeader, 4, 4);
        System.arraycopy(Util.IntToByteArray(totalSize), 0, byteHeader, 8, 4);
        System.arraycopy(Util.IntToByteArray(offset), 0, byteHeader, 12, 4);

        return byteHeader;
    }

    public void ImportHeader(byte[] data) {
        sessionID = Util.ByteArrayToInt(data, 0);
        packetType = GetPacketType(Util.ByteArrayToInt(data,4));
        totalSize = Util.ByteArrayToInt(data, 8);
        offset = Util.ByteArrayToInt(data, 12);
    }

    PacketType GetPacketType(int packetTypeOrdinal) {
        PacketType packetType = null;
        switch ( packetTypeOrdinal ) {
            case 0:
                packetType = PacketType.TRACKER_QUERY;
                break;
            case 1:
                packetType = PacketType.TRACKER_REGISTRATION;
                break;
            case 2:
                packetType = PacketType.TRACKER_QUERY_RESPONSE;
                break;
            case 3:
                packetType = PacketType.TRACKER_TORRENT_REGISTRATION;
                break;
            case 4:
                packetType = PacketType.CHUNK_LIST_REQUEST;
                break;
            case 5:
                packetType = PacketType.CHUNK_REQUEST;
                break;
            case 6:
                packetType = PacketType.CHUNK_LIST_RESPONSE;
                break;
            case 7:
                packetType = PacketType.CHUNK_RESPONSE;
                break;
            case 8:
                packetType = PacketType.TRACKER_TORRENT_QUERY;
                break;
            case 9:
                packetType = PacketType.TRACKER_TORRENT_RESPONSE;
                break;
        }
        return packetType;
    }
}
