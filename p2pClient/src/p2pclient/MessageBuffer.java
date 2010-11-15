package p2pclient;

/**
 * This is a utility class for tracking and assembling messages from message
 * chunks. It is a helper class for MessageReceive.
 *
 * @author Jeremy
 */
public class MessageBuffer {
    public Peer peer;
    public int sessionID;
    private PacketHeader packetHeader;
    private byte[] messageData;
    private int totalBytesReceived;
    private long timestampLastUpdatedMsec;
    private long timeoutValueMsec = 10000;

    MessageBuffer(Peer peer, PacketHeader initialHeader) {
        // TODO
        // allocate initialHeader.totalSize for this.messageData
        this.packetHeader = initialHeader;
        this.peer = peer;
        this.timestampLastUpdatedMsec = Util.GetCurrentTime();
        this.sessionID = initialHeader.sessionID;
        this.totalBytesReceived = 0;
        this.messageData = new byte[this.packetHeader.totalSize];
    }

    public void AddDataToMessage(PacketHeader incomingHeader, byte[] incomingData) {
        // TODO: copy incomingData to the incomingHeader.offset in this.messageData
        this.totalBytesReceived += incomingData.length;
        this.timestampLastUpdatedMsec = Util.GetCurrentTime();
        System.arraycopy(incomingData, 0, this.messageData, incomingHeader.offset, incomingData.length);
    }

    /**
     * Returns the header and message data for this message if it has been completely received
     *
     * @param peer[out]
     * @param packetType[out]
     * @param sessionID[out]
     *
     * @return message data if message is complete (output parameters are valid), null otherwise
     */
    public byte[] IsMessageComplete(Peer[] peer, PacketType[] packetType, int[] sessionID) {
        Thread.yield();
        if ( this.totalBytesReceived == this.packetHeader.totalSize ) {
            System.out.println("This message is complete! " + this.packetHeader.packetType + " " + this.packetHeader.sessionID + " " + this.totalBytesReceived);
            System.out.println(": " + Util.ConvertToHex(this.messageData));
            if ( peer != null ) {
                peer[0] = this.peer;
            }
            if ( packetType != null ) {
                packetType[0] = this.packetHeader.packetType;
            }
            if ( sessionID != null ) {
                sessionID[0] = this.packetHeader.sessionID;
            }
            return this.messageData;
        } else {
            return null;
        }
    }

    /**
     * Determines if this incomplete message has timed out and the holder should
     * discard it
     *
     * @param currentTimeMsec Current timestamp (msec)
     * @return true if it has timed out, false otherwise
     */
    public boolean IsMessageTimedOut(long currentTimeMsec) {
        if ( currentTimeMsec - this.timestampLastUpdatedMsec > this.timeoutValueMsec ) {
            return true;
        } else {
            return false;
        }
    }

}
