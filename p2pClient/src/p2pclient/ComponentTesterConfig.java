package p2pclient;

/**
 *
 * @author Jeremy
 */
public class ComponentTesterConfig {
//    public static String TEST_FILE_PATH_ROOT = "Z:/Class/CS657-ComputerNetworks/Git/CS657-MB-BK-JT/p2pClient/src/p2pclient/TestFilesDir/";
    public static String TEST_FILE_PATH_ROOT = "D:/GradSchool/C657-Networking/Project/GitHubRepo/p2pClient/src/p2pclient/TestFilesDir/";
    
    public static boolean TEST_LIST_REMOVAL_ALGORITHM = false;
    public static boolean TEST_UTIL_EXTRACT_NULL_TERMINATED_STRING = false;
    public static boolean TEST_TORRENT = true;
    public static boolean TEST_TRACKER_REGISTRATION_IMPORT_EXPORT = false;
    public static boolean TEST_TRACKER_QUERY_IMPORT_EXPORT = false;
    public static boolean TEST_TRACKER_QUERY_RESPONSE_IMPORT_EXPORT = false;
    public static boolean TEST_TRACKER_TORRENT_REGISTRATION = false;
    public static boolean TEST_TRACKER_TORRENT_QUERY = false;
    public static boolean TEST_TRACKER_TORRENT_QUERY_RESPONSE = false;
    
    public static boolean TEST_CHUNK_LIST_REQUEST_IMPORT_EXPORT = false;
    public static boolean TEST_CHUNK_LIST_RESPONSE_IMPORT_EXPORT = false;
    public static boolean TEST_CHUNK_REQUEST_IMPORT_EXPORT = false;
    public static boolean TEST_CHUNK_RESPONSE_IMPORT_EXPORT = false;
    public static boolean TEST_SERVING_CLIENT = false;
    public static boolean TEST_HOST = true;

    public static boolean TEST_TRACKER_WITH_REAL_SOCKETS = false;

    public static boolean[] DebugLevels = {
        true, //    ALL,
        false, //    CHUNK_INFO,
        false, //    CHUNK_LIST_REQUEST,
        false, //    CHUNK_LIST_RESPONSE,
        false, //    CHUNK_MANAGER,
        false, //    CHUNK_REQUEST,
        false, //    CHUNK_RESPONSE,
        true, //    COMPONENT_TESTER,
        false, //    FILE_CHUNK,
        false, //    FILE_RECEIVER,
        false, //    FILE_SENDER,
        true, //    HOST,
        false, //    LIST_OF_FILES,
        false, //    MESSAGE_BUFFER,
        false, //    MESSAGE_RECEIVE,
        false, //    MESAGE_SEND,
        false, //    PACKET_HEADER,
        false, //    PACKET_TYPE,
        false, //    PEER,
        false, //    PEER_MANAGER,
        false, //    REGISTERED_PEER,
        false, //    REQUESTING_CLIENT,
        false, //    SERVING_CLIENT,
        false, //    TORRENT,
        false, //    TRACKER,
        false, //    TRACKER_QUERY,
        false, //    TRACKER_QUERY_RESPONSE,
        false, //    TRACKER_REGISTRATION,
        true, //    TRACKER_TORRENT_QUERY,
        false, //    TRACKER_TORRENT_REGISTRATION,
        true, //    TRACKER_TORRENT_RESPONSE,
        false, //    UTIL
    };
}
