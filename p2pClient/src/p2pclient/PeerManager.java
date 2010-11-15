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
        if ( (peerToUpdate = FindPeerInList(peerToUpdate)) == null ) {
            AddPeerToList(peerToUpdate);
        }

    }

    private void AddPeerToList(Peer peerToAdd) {
        if ( peerList == null ) {
            peerList = new Peer[1];
        } else {
            peerList = new Peer[peerList.length + 1];
        }
        peerList[peerList.length - 1] = peerToAdd;
    }

    public void AddCreditForUsToPeer(Peer peerToUpdate) {
        Peer peer = FindPeerInList(peerToUpdate);

        peer.creditForUs++;
    }

    public void AddCreditForThemToPeer(Peer peerToUpdate) {
        Peer peer = FindPeerInList(peerToUpdate);

        peer.creditForThem++;
    }

    private Peer FindPeerInList(Peer peerToFind) {
        return null;
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

    public boolean ShouldTradeWith(Peer peer) {
        //TODO: Implement algorithm to determine if we want to share with the given peer or not
        return false;
    }

}
