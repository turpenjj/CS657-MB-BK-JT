/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;
/**
 * Description:
 *  This class is used to manage all the peers the host is aware of.  It is used to determine which peers we are willing to trade
 *  with and which peers have chunks that we need.
 * 
 * @author Matt
 */
public class PeerManager {
    Peer[] peerList;
    private static int PEER_UNCHOKE = 30000;

    PeerManager() {
        peerList = null;
        PEER_UNCHOKE = 30000;
    }

    /*
     * Description:
     *   Updates (adds if missing) the peer information in the PeerManager
     */
    public synchronized Peer UpdatePeer(Peer peerToUpdate) {
        Peer peerInList = GetPeer(peerToUpdate.clientIp);
        if ( peerInList == null ) {
            peerInList = new Peer(peerToUpdate.clientIp, peerToUpdate.listeningPort);
            AddPeerToList(peerInList);
        }
        return peerInList;
    }

    private synchronized void AddPeerToList(Peer peerToAdd) {
        if ( peerList == null ) {
            peerList = new Peer[1];
        } else {
            Peer[] tempList = new Peer[peerList.length + 1];
            System.arraycopy(peerList, 0, tempList, 0, peerList.length);
            peerList = tempList;
        }
        peerList[peerList.length - 1] = peerToAdd;
    }

    /*
     * Description:
     *   Gets a list of all peers that claim to be sharing chunks of the given
     *   file
     */
    public synchronized Peer[] GetAllPeersSharingFile(String filename) {
        Peer[] sharingPeers = null;

        if ( peerList != null ) {
            for (int i = 0; i < peerList.length; i++) {
                if ( peerList[i].HasFile(filename) ) {
                    sharingPeers = AddPeerToList(sharingPeers, peerList[i]);
                }
            }
        }

        return sharingPeers;
    }

    private synchronized Peer[] AddPeerToList(Peer[] list, Peer peer) {
        if ( list == null ) {
            list = new Peer[1];
        } else {
            Peer[] tempList = new Peer[list.length + 1];
            System.arraycopy(list, 0, tempList, 0, list.length);
            list = tempList;
        }
        list[list.length - 1] = peer;
        return null;
    }

    /*
     * Description:
     *   Returns a list of all peers that have the given chunk of a given file
     */
    public synchronized Peer[] GetPeersWithChunk(String filename, int chunkNumber) {
        Peer[] sharingPeers = null;

        if ( peerList != null ) {
            for (int i = 0; i < peerList.length; i++) {
                if ( peerList[i].HasChunk(filename, chunkNumber) ) {
                    sharingPeers = AddPeerToList(sharingPeers, peerList[i]);
                }
            }
        }

        return sharingPeers;
    }

    /*
     * Description:
     *   Returns the peer for the given IP.
     */
    public synchronized Peer GetPeer(InetAddress ipAddress) {
        if ( peerList != null ) {
            for (int i = 0; i < peerList.length; i++ ) {
                if ( peerList[i].clientIp == ipAddress) {
                    return peerList[i];
                }
            }
        }
        return null;
    }

    /*
     * Determines if the peer is worthy of our time.  Three factors determine
     * if we'll play nice.
     *
     * 1) If they have more credit than the amount we think they have for us
     * 2) If we have outstanding requests to them that is greater than the
     *    amount of credit we have more than them
     * 3) If we haven't serviced a request from this host for awhile, we'll
     *    service one request to potentially unchoke them.
     */
    public synchronized boolean ShouldTradeWith(Peer peer) {
        //TODO: Implement algorithm to determine if we want to share with the given peer or not
        int THROTTLE = 3;
        if ( (peer.creditForThem + THROTTLE) >= peer.creditForUs ||
                peer.outstandingRequests > (peer.creditForThem - peer.creditForUs) ||
                peer.lastServiced < (Util.GetCurrentTime() - PEER_UNCHOKE) ) {
                return true;
        }
        return false;
    }

}
