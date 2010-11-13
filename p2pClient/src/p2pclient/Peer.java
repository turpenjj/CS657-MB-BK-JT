/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class Peer {
    String clientName;
    int clientIp;
    int listeningPort;

    Peer(String name, int ip, int port) {
        clientName = name;
        clientIp = ip;
        listeningPort = port;
    }
}
