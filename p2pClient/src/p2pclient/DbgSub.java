package p2pclient;

/**
 * Defines the various debug subsystems in the package. Each call to
 * Util::DebugPrint() takes one of these as an argument and depending on the
 * configuration specified in ComponentTesterConfig the output may or may not
 * be displayed.
 * 
 * @author Jeremy
 */
public enum DbgSub {
    ALL,
    CHUNK_INFO,
    CHUNK_LIST_REQUEST,
    CHUNK_LIST_RESPONSE,
    CHUNK_MANAGER,
    CHUNK_REQUEST,
    CHUNK_RESPONSE,
    COMPONENT_TESTER,
    FILE_CHUNK,
    FILE_RECEIVER,
    FILE_SENDER,
    HOST,
    LIST_OF_FILES,
    MESSAGE_BUFFER,
    MESSAGE_RECEIVE,
    MESSAGE_SEND,
    PACKET_HEADER,
    PACKET_TYPE,
    PEER,
    PEER_MANAGER,
    REGISTERED_PEER,
    REQUESTING_CLIENT,
    SERVING_CLIENT,
    TORRENT,
    TRACKER,
    TRACKER_QUERY,
    TRACKER_QUERY_RESPONSE,
    TRACKER_REGISTRATION,
    TRACKER_TORRENT_QUERY,
    TRACKER_TORRENT_REGISTRATION,
    TRACKER_TORRENT_RESPONSE,
    UTIL
}