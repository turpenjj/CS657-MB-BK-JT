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
    }

    public void AddDataToMessage(PacketHeader incomingHeader, byte[] incomingData) {
        // TODO: copy incomingData to the incomingHeader.offset in this.messageData
        this.totalBytesReceived += incomingData.length;
        this.timestampLastUpdatedMsec = Util.GetCurrentTime();
    }

    /**
     * Returns the header and message data for this message if it has been completely received
     *
     * @param peer[out]
     * @param packetType[out]
     * @param sessionID[out]
     * @param messageData[out]
     *
     * @return true if message is complete (output parameters are valid), false otherwise
     */
    public boolean IsMessageComplete(Peer peer, PacketType packetType, int[] sessionID, byte[] messageData) {
        if ( this.totalBytesReceived == this.packetHeader.totalSize ) {
            if ( peer != null ) {
                peer = this.peer;
            }
            if ( packetType != null ) {
                packetType = this.packetHeader.packetType;
            }
            if ( peer != null ) {
                sessionID[0] = this.packetHeader.sessionID;
            }
            messageData = this.messageData;

            return true;
        } else {
            return false;
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
