/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

/**
 *
 * @author Matt
 */
public class ChunkHeader {
    int sessionID;
    PacketType packetType;
    int totalSize;
    int offset;
}