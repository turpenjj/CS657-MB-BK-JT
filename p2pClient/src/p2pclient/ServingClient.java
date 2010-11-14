/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.net.*;

/**
 * Description:
 *  This class is the client program running on a p2p Host that receives,
 *  processes, and responds to requests for chunk lists and chunks.
 *
 * @author Matt
 */
public class ServingClient implements Runnable {
    Thread runner;
    int listeningPort;
    DatagramSocket listeningSocket;

    ServingClient(int port) {
        listeningPort = port;
    }

    //Creates and starts a new ServingClient thread;
    public void start() {
        if ( runner == null ) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void run() {
        System.out.println("Started a new thread for ServingClient");
    }

    /*
     * Description:
     *   Creates and binds a socket for receiving new requests
     */
    private void CreateListener() {

    }

    /*
     * Description:
     *   Get the amount of "credit" the peer has to determine if the peer is worth
     *   our time
     */
    private int GetPeerCredit() {
        int peerCredit = 0;

        return peerCredit;
    }

    /*
     * Description:
     *   Responds to a Chunk List Request
     */
    private void HandleChunkListRequest() {
        
    }

    /*
     * Description:
     *   Responds to a Chunk Request
     */
    private void HandleChunkRequest() {
        
    }

    /*
     * Description:
     *   Tells this ServingClient thread to stop.
     */
    public void Stop() {

    }
}
