package p2pclient;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Arrays;

/**
 * Provides unit-level and higher tests for various other classes in the package.
 *
 * @note Which tests get executed is controlled by the configuration values in
 * ComponentTesterConfig, as well as the path for certain test files used by
 * these tests.
 *
 * @author Jeremy
 */
public class ComponentTester {
    
    public static void main (String args[]) throws Exception {
        /**
         * NOTE: try to test all the simplest pieces first, the the subcomponents,
         * then the higher-level components.
         */

        if (ComponentTesterConfig.TEST_SEND_RECEIVE_LARGE_MESSAGES) {
            TestSendReceiveLargeMessages();
        }
        if (ComponentTesterConfig.TEST_HOST) {
            TestHost();
        }
        if (ComponentTesterConfig.TEST_LIST_REMOVAL_ALGORITHM) {
            TestListRemovalAlgorithm();
        }
        if (ComponentTesterConfig.TEST_UTIL_EXTRACT_NULL_TERMINATED_STRING) {
            TestUtilExtractNullTerminatedString();
        }
        if (ComponentTesterConfig.TEST_TORRENT) {
            TestTorrent();
        }
        if (ComponentTesterConfig.TEST_TRACKER_REGISTRATION_IMPORT_EXPORT) {
            TestTrackerRegistrationImportExport();
        }
        if (ComponentTesterConfig.TEST_TRACKER_QUERY_IMPORT_EXPORT) {
            TestTrackerQueryImportExport();
        }
        if (ComponentTesterConfig.TEST_TRACKER_QUERY_RESPONSE_IMPORT_EXPORT) {
            TestTrackerQueryResponseImportExport();
        }
        if (ComponentTesterConfig.TEST_TRACKER_TORRENT_REGISTRATION) {
            TestTrackerTorrentRegistration();
        }
        if (ComponentTesterConfig.TEST_TRACKER_TORRENT_QUERY) {
            TestTrackerTorrentQuery();
        }
        if (ComponentTesterConfig.TEST_TRACKER_TORRENT_QUERY_RESPONSE) {
            TestTrackerTorrentQueryResponse();
        }
        if (ComponentTesterConfig.TEST_CHUNK_LIST_REQUEST_IMPORT_EXPORT) {
            TestChunkListRequestImportExport();
        }
        if (ComponentTesterConfig.TEST_CHUNK_LIST_RESPONSE_IMPORT_EXPORT) {
            TestChunkListResponseImportExport();
        }
        if (ComponentTesterConfig.TEST_CHUNK_REQUEST_IMPORT_EXPORT) {
            TestChunkRequestImportExport();
        }
        if (ComponentTesterConfig.TEST_CHUNK_RESPONSE_IMPORT_EXPORT) {
            TestChunkResponseImportExport();
        }
        if (ComponentTesterConfig.TEST_SERVING_CLIENT) {
            TestServingClient();
        }
        if (ComponentTesterConfig.TEST_TRACKER_WITH_REAL_SOCKETS) {
            TestTrackerWithRealSockets();
        }

        if (ComponentTesterConfig.TEST_CHUNK_MANAGER_FILE_WRITE) {
            TestChunkManagerFileWrite();
        }
    }

    /**
     * Tests the Chunk Manager's ability to write a file once all chunks have
     * been received.
     *
     * @throws Exception
     */
    private static void TestChunkManagerFileWrite() throws Exception {
        Torrent torrent = new Torrent(ComponentTesterConfig.TEST_FILE_PATH_ROOT, "TestFile.txt");
        ChunkManager chunkManager = new ChunkManager(torrent, ComponentTesterConfig.TEST_FILE_PATH_ROOT + File.separator + "OutputFiles");
        Peer samplePeer = new Peer(InetAddress.getLocalHost(), Util.GetRandomHighPort(new Random()));
        byte[] data = new byte[Torrent.CHUNK_SIZE];

        for (ChunkInfo chunkInfo : torrent.chunks) {
            System.arraycopy(torrent.fileDataTemp, chunkInfo.chunkNumber * Torrent.CHUNK_SIZE, data, 0, data.length);
            chunkManager.UpdateChunk(chunkInfo.chunkNumber, data, samplePeer);
        }

        byte[] writtenData = Util.ReadFileData(ComponentTesterConfig.TEST_FILE_PATH_ROOT + File.separator + "OutputFiles", torrent.filename);
        if (Arrays.equals(writtenData, torrent.fileDataTemp)) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Chunk manager wrote completed file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Chunk manager failed to write file properly");
        }
    }

    /**
     * Tests the MessageSend and MessageReceive ability to send and receive large
     * messages (made up of more than one packet).
     *
     * @throws Exception
     */
    private static void TestSendReceiveLargeMessages() throws Exception {
        PacketType[] acceptedPeerPacketTypes = {PacketType.CHUNK_RESPONSE};
        MessageReceive messageReceive = new MessageReceive(acceptedPeerPacketTypes, true);
        MessageSend messageSend = new MessageSend();
        Peer[] peer = new Peer[1];
        PacketType[] packetType = new PacketType[1];
        int[] sessionID = new int[1];
        byte[] messageData;
        byte[] data = new byte[5000];
        Random rand = new Random();
        Peer ourListener = new Peer(InetAddress.getLocalHost(), messageReceive.listeningPort);
        
        messageReceive.start();

        rand.nextBytes(data);

        messageSend.SendCommunication(ourListener, PacketType.CHUNK_RESPONSE, 1, data);

        Thread.sleep(1000);

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "About to call GetMessage");
        messageData = messageReceive.GetMessage(1, acceptedPeerPacketTypes, peer, packetType, sessionID);
        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Returned from call to GetMessage");
        if (messageData != null) {
            if (Arrays.equals(messageData,data)) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Sent and received a " + data.length + " byte message");
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Received message did not match sent message");
            }
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed to receive message");
        }

        messageReceive.Stop();
    }

    /**
     * Tests the Tracker using real sockets (through MessageSend and MessageReceive).
     *
     * @throws Exception
     */
    private static void TestTrackerWithRealSockets() throws Exception {
        Tracker tracker = new Tracker(Tracker.TRACKER_LISTENING_PORT);
        PacketType[] acceptedPeerPacketTypes = {
            PacketType.CHUNK_LIST_REQUEST, PacketType.CHUNK_REQUEST};
        PacketType[] acceptedTrackerResponsePacketTypes = {
            PacketType.TRACKER_QUERY_RESPONSE, PacketType.TRACKER_TORRENT_RESPONSE};
        Peer[] receivedPeer = new Peer[1];
        PacketType[] receivedPacketType = new PacketType[1];
        int[] receivedSessionId = new int[1];
        byte[] receivedMessageData;
        TrackerRegistration trackerRegistration;
        tracker.start();
        Peer trackerPeer = new Peer(InetAddress.getLocalHost(), Tracker.TRACKER_LISTENING_PORT);
        Random random = new Random();
        MessageSend messageSender = new MessageSend();
        MessageReceive messageReceive;
        MessageReceive queryMessageReceive;
        TrackerQuery trackerQuery;
        TrackerQueryResponse trackerQueryResponse;
        TrackerTorrentRegistration torrentRegistration;
        Torrent torrent;
        TrackerTorrentQuery trackerTorrentQuery;
        TrackerTorrentResponse trackerTorrentResponse;
        int sessionId;
        int numFiles;
        int fileNum;
        String fileSharePath;
        MessageReceive[] registeredListeners = new MessageReceive[0];
        MessageReceive[] temptRegisteredListeners;

        // give the tracker a chance to get going
        Thread.sleep(2000);

        for (int i = 0; i < 5; i++) {
            trackerRegistration = new TrackerRegistration(Util.GetRandomHighPort(random));
            messageReceive = new MessageReceive(trackerRegistration.peer.listeningPort, acceptedPeerPacketTypes, true);
            messageReceive.start();

            // add to list so we can stop them at the end
            temptRegisteredListeners = new MessageReceive[registeredListeners.length + 1];
            temptRegisteredListeners[0] = messageReceive;
            System.arraycopy(registeredListeners, 0, temptRegisteredListeners, 1, registeredListeners.length);
            registeredListeners = temptRegisteredListeners;

            if (i % 2 == 0) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "i = " + i);
                fileSharePath = ComponentTesterConfig.TEST_FILE_PATH_ROOT + "SetTestFileSet/";
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "i = " + i);
                fileSharePath = ComponentTesterConfig.TEST_FILE_PATH_ROOT + "TestTestFileSet/";
            }
            trackerRegistration.AddFilesFromDirectory(fileSharePath);
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Registering peer: " + trackerRegistration.toString());
            //Thread.sleep(200);
            messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_REGISTRATION, random.nextInt(), trackerRegistration.ExportMessagePayload());

            Thread.sleep(200);

            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Tracker state: " + tracker.toString());
            
//            String[] temp = new String[trackerRegistration.registeredPeers[0].files.length - 1];
//            System.arraycopy(trackerRegistration.registeredPeers[0].files, 0, temp, 0, temp.length);
//            trackerRegistration.registeredPeers[0].files = temp;
            // try out duplicate registrations (same peer with same listening port ot make sure it works)
            messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_REGISTRATION, random.nextInt(), trackerRegistration.ExportMessagePayload());

            Thread.sleep(500);

            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Tracker state: " + tracker.toString());
            
            for (String file : trackerRegistration.registeredPeers[0].files) {
                sessionId = random.nextInt();
                torrent = new Torrent(fileSharePath, file);
                torrentRegistration = new TrackerTorrentRegistration(torrent);
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Sent TRACKER_TORRENT_REGISTRATION message: Session = " + sessionId + "; " + torrentRegistration.registeredTorrents[0].toString());
                messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_TORRENT_REGISTRATION, sessionId, torrentRegistration.ExportMessagePayload());
            }

            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Tracker state: " + tracker.toString());

            for (String file : trackerRegistration.registeredPeers[0].files) {
                queryMessageReceive = new MessageReceive(Util.GetRandomHighPort(random), acceptedTrackerResponsePacketTypes, false);
                queryMessageReceive.start();
                trackerTorrentQuery = new TrackerTorrentQuery(file, queryMessageReceive.listeningPort);
                sessionId = random.nextInt();
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Sending TRACKER_TORRENT_QUERY message: Session = " + sessionId + "; Destination = " + trackerPeer.clientIp + ":" + trackerPeer.listeningPort + "; File = " + file + "; Response Expected Port =  " + queryMessageReceive.listeningPort);
                messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_TORRENT_QUERY, sessionId, trackerTorrentQuery.ExportQuery());

                Thread.sleep(200);

                receivedMessageData = queryMessageReceive.GetMessage(sessionId, acceptedTrackerResponsePacketTypes, receivedPeer, receivedPacketType, receivedSessionId);
                if (receivedMessageData != null) {
                    trackerTorrentResponse = new TrackerTorrentResponse();
                    trackerTorrentResponse.ImportMessagePayload(receivedMessageData);
                    
                    Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Received TRACKER_TORRENT_RESPONSE (session " + sessionId + ")");
                    Util.DebugPrint(DbgSub.COMPONENT_TESTER, trackerTorrentResponse.torrent.toString());

                    queryMessageReceive.Stop();
                } else {
                    Util.DebugPrint(DbgSub.COMPONENT_TESTER, "FAILED to receive TRACKER_TORRENT_RESPONSE (session " + sessionId + ")");
                    queryMessageReceive.Stop();
                }
            }

            // sleep a while but not long enough for our stuff to timeout
            Thread.sleep(TrackerRegistration.PEER_TIMEOUT_MSEC / 3);

            numFiles = trackerRegistration.registeredPeers[0].files.length;
            if (numFiles > 0) {
                fileNum = random.nextInt(numFiles);
                String filename = trackerRegistration.registeredPeers[0].files[fileNum];

                trackerQueryResponse = PerformTrackerQuery(trackerPeer, filename);

                // code to test if torrents are being deregistered properly, don't always want
                // to run since we have to wait for peers to timeout
                if (false) {
                    // let everything timeout
                    Thread.sleep(TrackerRegistration.PEER_TIMEOUT_MSEC * 100 / 99);

                    // should have timed out and an empty list returned
                    trackerQueryResponse = PerformTrackerQuery(trackerPeer, filename);

                    // tracker should have removed the torrent too since nobody is sharing it
                    Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Tracker state: " + tracker.toString());
                }
            }
        }

        for (MessageReceive messageReceiver : registeredListeners) {
            messageReceiver.Stop();
        }

        tracker.Stop();
    }

    /**
     * Helper function for TestTrackerWithRealSockets. Sends a peer query to
     * the given tracker and waits for a response.
     *
     * @param trackerPeer Tracker to send the request to
     * @param filename Name of file to search for peers that are sharing it
     * @return TrackerQueryReponse object representing the response, null if no response received
     */
    private static TrackerQueryResponse PerformTrackerQuery(Peer trackerPeer, String filename) {
        Random random = new Random();
        PacketType[] acceptedTrackerResponsePacketTypes = {PacketType.TRACKER_QUERY_RESPONSE};
        MessageReceive queryMessageReceive = new MessageReceive(Util.GetRandomHighPort(random), acceptedTrackerResponsePacketTypes, false);
        MessageSend messageSender = new MessageSend();
        queryMessageReceive.start();
        TrackerQuery trackerQuery = new TrackerQuery(filename, queryMessageReceive.listeningPort);
        int sessionId = random.nextInt();
        Peer[] receivedPeer = new Peer[1];
        PacketType[] receivedPacketType = new PacketType[1];
        int[] receivedSessionId = new int[1];
        byte[] receivedMessageData;
        TrackerQueryResponse trackerQueryResponse = null;

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Sending TRACKER_QUERY message: Session = " + sessionId + "; Destination = " + trackerPeer.clientIp + ":" + trackerPeer.listeningPort + "; File = " + filename + "; Response Expected Port =  " + queryMessageReceive.listeningPort);
        messageSender.SendCommunication(trackerPeer, PacketType.TRACKER_QUERY, sessionId, trackerQuery.ExportQuery());

        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Caught exception " + e);
        }

        receivedMessageData = queryMessageReceive.GetMessage(sessionId, acceptedTrackerResponsePacketTypes, receivedPeer, receivedPacketType, receivedSessionId);
        if (receivedMessageData != null) {
            trackerQueryResponse = new TrackerQueryResponse();
            trackerQueryResponse.ImportResponse(receivedMessageData);

            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Received TRACKER_RESPONSE (session " + sessionId + ")");
            for (Peer listPeer : trackerQueryResponse.peerList) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, listPeer.clientIp + ":" + listPeer.listeningPort);
            }
            queryMessageReceive.Stop();
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "FAILED to receive TRACKER_RESPONSE (session " + sessionId + ")");
            queryMessageReceive.Stop();
        }

        return trackerQueryResponse;
    }

    /**
     * Tests a code algorithm for removing an element from a list.
     *
     * @see RemoveFromList
     */
    private static void TestListRemovalAlgorithm() {
        Integer[] list = {0, 1, 2, 3, 4, 5};
        Integer[] removeList;
        Integer[] retList;

        Integer[] listRemoveFirst = {0};
        removeList = listRemoveFirst;
        retList = RemoveFromList(list, removeList);
        if (    retList.length != list.length - removeList.length ||
                IsIntegerInList(0, retList) ||
                !(  IsIntegerInList(1, retList) &&
                    IsIntegerInList(2, retList) &&
                    IsIntegerInList(3, retList) &&
                    IsIntegerInList(4, retList) &&
                    IsIntegerInList(5, retList) ) ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing first element in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed first element in list");
        }

        Integer[] listRemoveLast = {5};
        removeList = listRemoveLast;
        retList = RemoveFromList(list, removeList);
        if (    retList.length != list.length - removeList.length ||
                IsIntegerInList(5, retList) ||
                !(  IsIntegerInList(0, retList) &&
                    IsIntegerInList(1, retList) &&
                    IsIntegerInList(2, retList) &&
                    IsIntegerInList(3, retList) &&
                    IsIntegerInList(4, retList) ) ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing last element in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed last element in list");
        }

        Integer[] listRemoveMiddle = {3};
        removeList = listRemoveMiddle;
        retList = RemoveFromList(list, removeList);
        if (    retList.length != list.length - removeList.length ||
                IsIntegerInList(3, retList) ||
                !(  IsIntegerInList(0, retList) &&
                    IsIntegerInList(1, retList) &&
                    IsIntegerInList(2, retList) &&
                    IsIntegerInList(4, retList) &&
                    IsIntegerInList(5, retList) ) ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing middle element in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed middle element in list");
        }

        Integer[] listRemoveAll = list.clone();
        removeList = listRemoveAll;
        retList = RemoveFromList(list, removeList);
        if ( retList.length != list.length - removeList.length ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing all elements in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed all elements in list");
        }

        Integer[] listRemoveFirstAndLast = {0, 5};
        removeList = listRemoveFirstAndLast;
        retList = RemoveFromList(list, removeList);
        if (    retList.length != list.length - removeList.length ||
                IsIntegerInList(0, retList) ||
                IsIntegerInList(5, retList) ||
                !(  IsIntegerInList(1, retList) &&
                    IsIntegerInList(2, retList) &&
                    IsIntegerInList(3, retList) &&
                    IsIntegerInList(4, retList) ) ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing first and last element in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed first and last element in list");
        }

        Integer[] listRemoveDisparate = {1, 2, 4};
        removeList = listRemoveDisparate;
        retList = RemoveFromList(list, removeList);
        if (    retList.length != list.length - removeList.length ||
                IsIntegerInList(1, retList) ||
                IsIntegerInList(2, retList) ||
                IsIntegerInList(4, retList) ||
                !(  IsIntegerInList(0, retList) &&
                    IsIntegerInList(3, retList) &&
                    IsIntegerInList(5, retList) ) ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed removing disparate elements in list");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Removed disparate elements in list");
        }
    }

    /**
     * Helper function for TestListRemovalAlgorithm(). Determines if a given integer
     * is in a list of integers.
     *
     * @param integer Integer to search for
     * @param list List to search through (cannot be null)
     * @return true if integer was in list, false otherwise
     */
    private static boolean IsIntegerInList(Integer integer, Integer[] list) {
        for (Integer check : list) {
            if (check.equals(integer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Template code for removing an element from a list. This example uses a
     * list of integers and the criteria for removal is the element is in a list
     * of integers to remove.
     *
     * @param list List to remove from
     * @param listToRemove List to be removed
     * @return New list with specified elements removed
     */
    private static Integer[] RemoveFromList(Integer[] list, Integer[] listToRemove) {
        int currentIndex = 0;
        Integer[] tempList;

        for (Integer integer : list) {
            if (IsIntegerInList(integer, listToRemove)) {
                tempList = new Integer[list.length - 1];
                System.arraycopy(list, 0, tempList, 0, currentIndex);
                System.arraycopy(list, currentIndex + 1, tempList, currentIndex, tempList.length - currentIndex);
                list = tempList;
                continue;
            }
            currentIndex++;
        }

        return list;
    }

    /**
     * Tests the TrackerTorrentResponse class (generating and interpreting
     * response messages)
     *
     * @throws Exception
     */
    private static void TestTrackerTorrentQueryResponse() throws Exception {
        Torrent torrent = new Torrent(ComponentTesterConfig.TEST_FILE_PATH_ROOT, "TestFile2.txt");

        TrackerTorrentResponse ttqr = new TrackerTorrentResponse(torrent);
        byte[] messageData = ttqr.ExportMessagePayload();
        if (messageData != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: TrackerTorrentResponse::ExportMessagePayload succeeded");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: TrackerTorrentResponse::ExportMessagePayload failed");
        }

        TrackerTorrentResponse ttqrImport = new TrackerTorrentResponse();
        ttqrImport.ImportMessagePayload(messageData);
        if (ttqrImport.torrent != null &&
                ttqrImport.torrent.filename.contentEquals(torrent.filename) &&
                ttqrImport.torrent.filesize == torrent.filesize &&
                ttqrImport.torrent.numChunks == torrent.numChunks) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: TrackerTorrentResponse::ImportMessagePayload succeeded");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: TrackerTorrentResponse::ImportMessagePayload succeeded");
        }
    }

    /**
     * Tests the TrackerTorrentQuery class (generating and interpreting query
     * messages)
     *
     * @throws Exception
     */
    private static void TestTrackerTorrentQuery() throws Exception {
        TrackerTorrentRegistration[] trackerRegisteredTorrents = new TrackerTorrentRegistration[1];
        TrackerTorrentRegistration tracker;
        Torrent[] torrents;
        int listeningPort = 28000;

        torrents = RegisterTestTorrents(trackerRegisteredTorrents);
        tracker = trackerRegisteredTorrents[0];

        TrackerTorrentQuery trackerTorrentQuery = new TrackerTorrentQuery(torrents[0].filename, listeningPort);
        
        byte[] messageData = trackerTorrentQuery.ExportQuery();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + trackerTorrentQuery.filename +
                ", listeningPort = " + trackerTorrentQuery.listeningPort +
                ", messageData (" + messageData.length + ")");

        TrackerTorrentQuery trackerTorrentQueryImport = new TrackerTorrentQuery();

        if (trackerTorrentQueryImport.ImportQuery(messageData)) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Import reported success: Filename = " + trackerTorrentQueryImport.filename +
                    ", listeningPort = " + trackerTorrentQueryImport.listeningPort);
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Import reported failure");
        }
    }

    /**
     * Helper function for other tests. Registers a few test torrents with a
     * TrackerTorrentRegistration object.
     * 
     * @param tracker[out] Populated with the TrackerTorrentRegistration object
     * @return List of torrents registered
     *
     * @throws Exception
     */
    private static Torrent[] RegisterTestTorrents(TrackerTorrentRegistration[] tracker) throws Exception {
        TrackerTorrentRegistration trackerRegisteredTorrents = new TrackerTorrentRegistration();
        Torrent[] torrents;

        Torrent torrent;
        TrackerTorrentRegistration torrentRegistration;
        byte[] messageData;

        Torrent torrent2;
        TrackerTorrentRegistration torrentRegistration2;
        byte[] messageData2;

        torrent = new Torrent(ComponentTesterConfig.TEST_FILE_PATH_ROOT, "TestFile.txt");
        torrentRegistration = new TrackerTorrentRegistration(torrent);
        messageData = torrentRegistration.ExportMessagePayload();

        torrent2 = new Torrent(ComponentTesterConfig.TEST_FILE_PATH_ROOT, "TestFile2.txt");
        torrentRegistration2 = new TrackerTorrentRegistration(torrent2);
        messageData2 = torrentRegistration2.ExportMessagePayload();

        trackerRegisteredTorrents.ImportMessagePayload(messageData);
        trackerRegisteredTorrents.ImportMessagePayload(messageData2);

        torrents = trackerRegisteredTorrents.GetAllTorrents();

        if (tracker != null) {
            tracker[0] = trackerRegisteredTorrents;
        }

        return torrents;
    }

    /**
     * Tests the TrackerTorrentRegistration class (generating and interpreting
     * torrent registration messages and tracking those registered torrents).
     *
     * @throws Exception
     */
    private static void TestTrackerTorrentRegistration() throws Exception {
        TrackerTorrentRegistration[] trackerRegisteredTorrents = new TrackerTorrentRegistration[1];
        TrackerTorrentRegistration tracker;
        Torrent[] torrents;

        torrents = RegisterTestTorrents(trackerRegisteredTorrents);
        tracker = trackerRegisteredTorrents[0];

        if (tracker.Search(torrents[0].filename) != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed to find file");
        }
        if (tracker.Search(torrents[1].filename) != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed to find file");
        }
        if (tracker.Search(torrents[1].filename.toLowerCase()) != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed to find file");
        }
        if (tracker.Search("UnregisteredFile") == null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Didn't find file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Found file");
        }

        tracker.DeregisterTorrent(torrents[0]);
        if (tracker.Search(torrents[0].filename) == null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Didn't find file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Found file");
        }
        if (tracker.Search(torrents[1].filename) != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found file");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Failed to find file");
        }
    }

    /**
     * Tests the Torrent class.
     *
     * @throws Exception
     */
    private static void TestTorrent() throws Exception {
        Torrent torrent = new Torrent(ComponentTesterConfig.TEST_FILE_PATH_ROOT, "TestFile.txt");

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, torrent.toString());
    }

    /**
     * Tests the TrackerRegistration ability to receive peer registrations and
     * search through the registered peers.
     * 
     * @throws Exception
     */
    private static void TestTrackerRegistrationImportExport() throws Exception {
        TrackerRegistration trackerRegistration = new TrackerRegistration(21234);
        byte[] messageData;
        Peer[] peers;

        trackerRegistration.AddFilesFromDirectory("Test");

        messageData = trackerRegistration.ExportMessagePayload();
        if (messageData != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TestTrackerRegistration: ExportMessagePayload succeeded");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TestTrackerRegistration: ExportMessagePayload failed");
        }

        TrackerRegistration trackerRegistration2 = new TrackerRegistration(50123);
        byte[] messageData2;

        trackerRegistration2.AddFilesFromDirectory("Set");

        messageData2 = trackerRegistration2.ExportMessagePayload();
        if (messageData2 != null) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TestTrackerRegistration: ExportMessagePayload succeeded");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TestTrackerRegistration: ExportMessagePayload failed");
        }

        TrackerRegistration trackerRegistrationImport = new TrackerRegistration();
        int IPint;
        InetAddress IP;

        IPint = Util.InetAddressToInt(trackerRegistration.peer.clientIp);
        IP = InetAddress.getByAddress(Util.IntToByteArray(IPint + 1));
        Peer peer1 = new Peer(IP, 0);
        trackerRegistrationImport.ImportMessage(peer1, messageData);
        
        Thread.sleep((long)(trackerRegistrationImport.PEER_TIMEOUT_MSEC * 0.5));

        IP = InetAddress.getByAddress(Util.IntToByteArray(IPint + 2));
        Peer peer2 = new Peer(IP, 0);
        trackerRegistrationImport.ImportMessage(peer2, messageData2);

        Thread.sleep((long)(trackerRegistrationImport.PEER_TIMEOUT_MSEC * 0.6));

        peers = trackerRegistrationImport.Search("Test1");
        if (peers != null && peers.length != 0) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: 'Test' test set should have timed out");
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: 'Test' set timed out");
        }
        peers = trackerRegistrationImport.Search("Set1");
        if (peers == null || peers.length == 0) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: 'Set' test set should still be present");
        } else {
            if (peers[0].clientIp.equals(peer2.clientIp) &&
                    peers[0].listeningPort == trackerRegistration2.peer.listeningPort) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found 'Set1' file from the correct host");
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Found 'Set1' file but from incorrect host");
            }
        }

        // first instance should have timed out by now
        trackerRegistrationImport.ImportMessage(peer1, messageData);
        // this should be a replacement before the timeout occurs
        trackerRegistrationImport.ImportMessage(peer2, messageData2);

        peers = trackerRegistrationImport.Search("Test4");
        if (peers == null || peers.length == 0) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: 'Test' test set should still be present");
        } else {
            if (peers[0].clientIp.equals(peer1.clientIp) &&
                    peers[0].listeningPort == trackerRegistration.peer.listeningPort) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found 'Test4' file from the correct host");
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Found 'Test4' file but from incorrect host");
            }
        }
        peers = trackerRegistrationImport.Search("Set1");
        if (peers != null && peers.length == 0) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: 'Set' test set should still be present");
        } else {
            if (peers[0].clientIp.equals(peer2.clientIp) &&
                    peers[0].listeningPort == trackerRegistration2.peer.listeningPort) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Success: Found 'Set1' file from the correct host");
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Error: Found 'Set1' file but from incorrect host");
            }
        }

        trackerRegistration.AddFilesFromDirectory(ComponentTesterConfig.TEST_FILE_PATH_ROOT + "SetTestFileSet");
        trackerRegistration.AddFilesFromDirectory(ComponentTesterConfig.TEST_FILE_PATH_ROOT + "EmptyTestFileSet");
        trackerRegistration.AddFilesFromDirectory(ComponentTesterConfig.TEST_FILE_PATH_ROOT + "DirectoryDoesntExist");
    }

    /**
     * Tests the TrackerQuery class ability to generate and interpret peer query
     * messages.
     */
    private static void TestTrackerQueryImportExport() {
        String filename = "Testfilename.ext";
        int listeningPort = 44235;
        TrackerQuery trackerQuery = new TrackerQuery(filename, listeningPort);
        byte[] messageData = trackerQuery.ExportQuery();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + trackerQuery.filename +
                ", listeningPort = " + trackerQuery.listeningPort +
                ", messageData (" + messageData.length + ")");

        TrackerQuery trackerQueryImport = new TrackerQuery();

        if (trackerQueryImport.ImportQuery(messageData)) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Import reported success: Filename = " + trackerQueryImport.filename +
                    ", listeningPort = " + trackerQueryImport.listeningPort);
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Import reported failure");
        }
    }

    /**
     * Tests the TrackerQueryReponse class ability to generate and interpret
     * peer response messages.
     * 
     * @throws Exception
     */
    private static void TestTrackerQueryResponseImportExport() throws Exception {
        Peer[] peerList = new Peer[10];
        TrackerQueryResponse trackerQueryResponse;
        TrackerQueryResponse trackerQueryResponseImport;
        int i;
        InetAddress IP = InetAddress.getByName("192.168.1.1");
        byte[] startingIPbyte = IP.getAddress();
        int startingIPnumeric = Util.ByteArrayToInt(startingIPbyte);
        int currentIP = startingIPnumeric;
        int startingPort = 20000;
        int currentPort = startingPort;
        byte[] messageData;

        for (i = 0; i < peerList.length; i++) {
            peerList[i] = new Peer(IP, currentPort);
            currentIP += 3;
            currentPort += 127;
        }

        trackerQueryResponse = new TrackerQueryResponse(peerList);
        messageData = trackerQueryResponse.ExportResponse();

        trackerQueryResponseImport = new TrackerQueryResponse();
        if (trackerQueryResponseImport.ImportResponse(messageData)) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TrackerQueryReponse:Import reported success");

            i = 0;
            for (Peer peer : trackerQueryResponseImport.peerList) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, i + ": IP = " + peer.clientIp + "; Port = " + peer.listeningPort);
                i++;
            }
        } else {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "TrackerQueryReponse:Import failed");
        }

    }

    /**
     * Test the Util::ExtractNullTerminatedString function
     */
    private static void TestUtilExtractNullTerminatedString() {
        String test2 = "Firstteststring" + '\0' + "Secondteststring" + '\0' + "Thirdteststring";
        byte[] test2bytes = test2.getBytes();
        int currentIndex = 0;
        int[] nextIndex = {0};
        String string;
        int i;

        for (i = 0; i < 3; i++) {
            if ((string = Util.ExtractNullTerminatedString(test2bytes, currentIndex, nextIndex)) != null) {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Iteration " + i + ": String = " + string +
                        "; currentIndex = " + currentIndex + "; nextIndex = " + nextIndex[0]);
                currentIndex = nextIndex[0];
            } else {
                Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Iteration " + i + ": NULL");
            }
        }
    }

    /**
     * Tests the ChunkListRequest class ability to generate and interpret chunk
     * list request messages.
     */
    private static void TestChunkListRequestImportExport() {
        String filename = "Testfilename.ext";
        int listeningPort = 44234;
        ChunkListRequest request = new ChunkListRequest(filename, listeningPort);
        byte[] messageData = request.ExportMessagePayload();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + request.filename +
                ", listeningPort = " + request.receivingPort +
                ", messageData (" + messageData.length + ")");
        ChunkListRequest requestImport = new ChunkListRequest();

        requestImport.ImportMessagePayload(messageData);

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + requestImport.filename +
                ", listeningPort = " + requestImport.receivingPort +
                ", messageData (" + messageData.length + ")");
    }

    /**
     * Tests the ChunkListResponse class ability to generate and interpret chunk
     * list response messages.
     */
    private static void TestChunkListResponseImportExport() {
        String filename = "Testfilename.ext";
        ChunkInfo[] chunkInfoList = new ChunkInfo[5];
        byte[] chunkHash = {0x00, 0x00, 0x00, 0x01};
        for ( int i = 0; i < 5; i++ ) {
            chunkInfoList[i] = new ChunkInfo(i, chunkHash, 0);
        }
        ChunkListResponse response = new ChunkListResponse(filename, chunkInfoList);
        byte[] messageData = response.ExportMessagePayload();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + response.filename);
        for (int i = 0; i < response.chunkList.length; i++) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "chunk[" + i + "]");
        }
        ChunkListResponse responseImport = new ChunkListResponse();

        responseImport.ImportMessagePayload(messageData);

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + response.filename);
        for (int i = 0; i < response.chunkList.length; i++) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "chunk[" + i + "]");
        }
    }

    /**
     * Tests the ChunkRequest class ability to generate and interpret chunk
     * request messages.
     */
    private static void TestChunkRequestImportExport() {
        String filename = "TestFilename.ext";
        int chunkNumber = 1;
        int listeningPort = 54321;
        ChunkRequest request = new ChunkRequest(filename, chunkNumber, listeningPort);
        byte[] messageData = request.ExportMessagePayload();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + request.filename +
                ", listeningPort = " + request.listeningPort +
                ", messageData (" + messageData.length + ")");

        ChunkRequest requestImport = new ChunkRequest();
        requestImport.ImportMessagePayload(messageData);

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + requestImport.filename +
                ", listeningPort = " + requestImport.listeningPort +
                ", messageData (" + messageData.length + ")");
    }

    /**
     * Tests the ChunkResponse class ability to generate and interpret chunk
     * response messages.
     */
    private static void TestChunkResponseImportExport() {
        String filename = "TestFilename.ext";
        int chunkNumber = 1;
        int listeningPort = 54321;
        byte[] chunkData = {0, 1, 2, 3, 4, 5};
        ChunkResponse response = new ChunkResponse(filename, chunkNumber, chunkData);
        byte[] messageData = response.ExportMessagePayload();

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + response.filename +
                ", chunkNumber = " + response.chunkNumber +
                ", chunkData (" + response.chunkData.length + ")" +
                ", messageData (" + messageData.length + ")");

        ChunkResponse responseImport = new ChunkResponse();
        responseImport.ImportMessagePayload(messageData);

        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Filename = " + responseImport.filename +
                ", chunkNumber = " + responseImport.chunkNumber +
                ", chunkData (" + responseImport.chunkData.length + ")" +
                ", messageData (" + messageData.length + ")");
    }

    /**
     * Tests the Host class
     */
    private static void TestHost() {
        try {
            Host newHost = new Host(13454, ComponentTesterConfig.TEST_FILE_PATH_ROOT, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "Caught exception " + e);
        }
    }

    /**
     * Tests the ServingClient class
     */
    private static void TestServingClient() {
        int listeningPort = 54321;
        ChunkManager[] chunkManagers = new ChunkManager[5];
        ChunkManager[] reqchunkManagers = new ChunkManager[5];
        PeerManager peerManager = new PeerManager();

        //Initialize chunkManagers;
        for ( int i = 0; i < 5; i++) {
            chunkManagers[i] = new ChunkManager("Filename" + i, ComponentTesterConfig.TEST_FILE_PATH_ROOT);
            chunkManagers[i].chunkList = new FileChunk[2];
            byte[] tempHash = {(byte)i,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9};
            chunkManagers[i].chunkList[0] = new FileChunk(0, tempHash, 0, null);
            byte[] tempHash2 = {0,(byte)i,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9};
            byte[] chunkPiece = {1,2,3,2,1,0};
            chunkManagers[i].chunkList[1] = new FileChunk(1, tempHash2, 2, chunkPiece);
            chunkManagers[i].chunkList[1].chunk = new byte[3];
            chunkManagers[i].chunkList[1].chunk[0] = (byte)i;
            chunkManagers[i].chunkList[1].chunk[1] = (byte)2;
            chunkManagers[i].chunkList[1].chunk[2] = (byte)i;
        }
        //Initialize Requestor chunkManagers
        for ( int i = 0; i < 5; i++) {
            reqchunkManagers[i] = new ChunkManager("Filename" + i, ComponentTesterConfig.TEST_FILE_PATH_ROOT);
            reqchunkManagers[i].chunkList = new FileChunk[2];
            byte[] tempHash = {(byte)i,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9};
            reqchunkManagers[i].chunkList[0] = new FileChunk(0, tempHash, 0, null);
            byte[] tempHash2 = {0,(byte)i,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9};
            reqchunkManagers[i].chunkList[1] = new FileChunk(1, tempHash2, 0, null);
    }

        ServingClient servingClient = new ServingClient(listeningPort, chunkManagers, peerManager);
        servingClient.start();
//        try {
//            Thread.sleep(2000);
//        } catch ( InterruptedException e ) {
//
//        }

        try {
            Peer dummyPeer = new Peer(InetAddress.getLocalHost(), 51515);
            peerManager.UpdatePeer(dummyPeer);
            Peer peer = new Peer(InetAddress.getLocalHost(), 54321);
            RequestingClient requestingClient = new RequestingClient(peer, "Filename1", chunkManagers[1], peerManager);
            requestingClient.start();
        } catch ( UnknownHostException e ) {
            Util.DebugPrint(DbgSub.COMPONENT_TESTER, "We're in trouble... we can't find ourself " + e);
        }
//        Util.DebugPrint(DbgSub.COMPONENT_TESTER, "peerList (" + peerManager.peerList.length + ")");
    }
}
