/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 * Description:
 *  This class is used to manage all the peers the host is aware of.  It is used to determine which peers we are willing to trade
 *  with and which peers have chunks that we need.
 * 
 * @author Matt
 */
public class PeerManager {
    Peer[] peerList;

    PeerManager() {
        //Null constructor, since PeerManager will start out empty
    }

    /*
     * Description:
     *   Updates (adds if missing) the peer information in the PeerManager
     */
    public void UpdatePeer(Peer peerToUpdate) {

    }

    private int FindPeerInList(Peer peerToFind) {
        return 0;
    }

    /*
     * Description:
     *   Gets a list of all peers that claim to be sharing chunks of the given
     *   file
     */
    public Peer[] GetAllPeersSharingFile(String filename) {
        Peer[] sharingPeers = null;

        return sharingPeers;
    }

    /*
     * Description:
     *   Returns a list of all peers that have the given chunk of a given file
     */
    public Peer[] GetPeersWithChunk(String filename, int chunkNumber) {
        return null;
    }

    /*
     * Description:
     *   Returns the peer for the given IP.
     */
    public Peer GetPeer(int ip) {
        return null;
    }

}
