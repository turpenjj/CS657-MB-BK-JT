/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;


/**
 *
 * @author Matt
 */
public enum PacketType {
    TRACKER_QUERY,
    TRACKER_REGISTRATION,
    TRACKER_QUERY_RESPONSE,
    TRACKER_TORRENT_REGISTRATION,
    CHUNK_LIST_REQUEST,
    CHUNK_REQUEST,
    CHUNK_LIST_RESPONSE,
    CHUNK_RESPONSE,
    TRACKER_TORRENT_QUERY,
    TRACKER_TORRENT_RESPONSE
}
