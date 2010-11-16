package p2pclient;

/**
 * Defines a data tracking class for a peer and the list of files it has
 * registered as sharing.
 *
 * @author Jeremy
 */
public class RegisteredPeer {
    public Peer peer; // peer details
    public String[] files; // list of filenames being shared
    public long timestampMsec; //registation timestamp

    RegisteredPeer(Peer peer, String[] files) {
        this.peer = peer;
        this.timestampMsec = Util.GetCurrentTime();

        if (files != null) {
            this.files = new String[files.length];
            System.arraycopy(files, 0, this.files, 0, files.length);
        } else {
            this.files = new String[0];
        }
    }

    public String toString() {
        String string = this.peer.toString() + "; Timestamp = " + this.timestampMsec + "; Files = ";

        for (String file : this.files) {
            string = string.concat(file + "; ");
        }

        return string;
    }
}
