package p2pclient;

/**
 * This class defines the structure of the tracker query message and provides methods for creating such messages.
 *
 * @author Jeremy
 */
public class TrackerQuery {
    PacketHeader ch;
    String filename;

    /**
     * Would expect a client to call this.
     * @param filename
     */
    public void SetFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Would expect the tracker to call this after calling ImportQuery.
     * 
     * @return
     */
    public String GetFilename() {
        return this.filename;
    }

    /**
     * Returns the query structure as it should exist on the wire. Would expect the tracker to call this.
     */
    public byte[] ExportQuery() {
        return null;
    }

    /**
     * Initialize an instance from a data byte stream. Would expect the client to call this.
     * 
     * @param data
     * @return
     */
    public boolean ImportQuery(byte[] data) {
        // read the filename out and set the internal data member
        return true;
    }
}
