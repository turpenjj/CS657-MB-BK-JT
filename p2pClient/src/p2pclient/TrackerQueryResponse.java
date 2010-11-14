package p2pclient;

/**
 * This class defines the structures for the tracker query response message and provides methods for creating such messages.
 * @author Jeremy
 */
public class TrackerQueryResponse {
    PacketHeader ch;
    Peer[] clientList;


    /**
     * Would expect the tracker to call this with a list returned by the TrackerRegistration class
     * @param peerList that has chunks for a given file
     */
    public void SetPeerList(Peer[] peerList) {
        this.clientList = peerList;
    }

    /**
     * Would expect a client to call this to get a list of Peers after calling ImportResponse on a response returned by the server.
     * @return list of Peers to attempt to get chunks from
     */
    public Peer[] GetPeerList() {
        return this.clientList;
    }

    /**
     * Would expect the tracker to call this.
     *
     * Returns the query response structure as it should exist on the wire, which is simply a list of Peers
     */
    public byte[] ExportReponse() {
        return null;
    }

    /**
     * Would expect a client to call this. Initialize an instance from a data byte stream.
     *
     * @param data Byte stream from the wire
     * @return
     */
    public boolean ImportReponse(byte[] data) {
        // read the list of peers out of the data and set the internal members
        return true;
    }
}
