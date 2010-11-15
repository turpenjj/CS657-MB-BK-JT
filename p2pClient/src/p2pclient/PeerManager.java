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

    PeerManager() {
        peerList = null;
    }

    /*
     * Description:
     *   Updates (adds if missing) the peer information in the PeerManager
     */
    public synchronized Peer UpdatePeer(Peer peerToUpdate) {
        Peer peerInList = GetPeer(peerToUpdate.clientIp);
        if ( peerInList == null ) {
            peerInList = new Peer(peerToUpdate.clientIp, 0);
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

    public synchronized boolean ShouldTradeWith(Peer peer) {
        //TODO: Implement algorithm to determine if we want to share with the given peer or not
        return false;
    }

}
