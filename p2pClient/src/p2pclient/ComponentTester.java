package p2pclient;

import java.io.*;
import java.net.*;

/**
 *
 * @author Jeremy
 */
public class ComponentTester {
    public static String TEST_FILE_PATH_ROOT = "Z:/Class/CS657-ComputerNetworks/Git/TestFilesDir/";

    public static void main (String args[]) throws Exception {
        TestChunkListRequestImportExport();
        TestChunkListResponseImportExport();
        TestChunkRequestImportExport();
        TestChunkResponseImportExport();
//        TestTrackerRegistrationImportExport();
//        TestTrackerQueryResponseImportExport();
//        TestTrackerQueryImportExport();
//        TestUtilExtractNullTerminatedString();
    }

    private static void TestTrackerRegistrationImportExport() throws Exception {
        TrackerRegistration trackerRegistration = new TrackerRegistration(21234);
        byte[] messageData;
        Peer[] peers;

        trackerRegistration.AddFilesFromDirectory("Test");

        messageData = trackerRegistration.ExportMessagePayload();
        if (messageData != null) {
            System.out.println("TestTrackerRegistration: ExportMessagePayload succeeded");
        } else {
            System.out.println("TestTrackerRegistration: ExportMessagePayload failed");
        }

        TrackerRegistration trackerRegistration2 = new TrackerRegistration(50123);
        byte[] messageData2;

        trackerRegistration2.AddFilesFromDirectory("Set");

        messageData2 = trackerRegistration2.ExportMessagePayload();
        if (messageData2 != null) {
            System.out.println("TestTrackerRegistration: ExportMessagePayload succeeded");
        } else {
            System.out.println("TestTrackerRegistration: ExportMessagePayload failed");
        }

        TrackerRegistration trackerRegistrationImport = new TrackerRegistration();
        Peer peer1 = new Peer(trackerRegistration.peer.clientIp + 1, 0);
        Peer peer2 = new Peer(trackerRegistration2.peer.clientIp + 2, 0);
        trackerRegistrationImport.ImportMessage(peer1, messageData);
        
        Thread.sleep((long)(trackerRegistrationImport.PEER_TIMEOUT_MSEC * 0.5));

        trackerRegistrationImport.ImportMessage(peer2, messageData2);

        Thread.sleep((long)(trackerRegistrationImport.PEER_TIMEOUT_MSEC * 0.6));

        peers = trackerRegistrationImport.Search("Test1");
        if (peers != null && peers.length != 0) {
            System.out.println("Error: 'Test' test set should have timed out");
        } else {
            System.out.println("Success: 'Test' set timed out");
        }
        peers = trackerRegistrationImport.Search("Set1");
        if (peers == null || peers.length == 0) {
            System.out.println("Error: 'Set' test set should still be present");
        } else {
            if (peers[0].clientIp == trackerRegistration2.peer.clientIp + 2 &&
                    peers[0].listeningPort == trackerRegistration2.peer.listeningPort) {
                System.out.println("Success: Found 'Set1' file from the correct host");
            } else {
                System.out.println("Error: Found 'Set1' file but from incorrect host");
            }
        }

        // first instance should have timed out by now
        trackerRegistrationImport.ImportMessage(peer1, messageData);
        // this should be a replacement before the timeout occurs
        trackerRegistrationImport.ImportMessage(peer2, messageData2);

        peers = trackerRegistrationImport.Search("Test4");
        if (peers == null || peers.length == 0) {
            System.out.println("Error: 'Test' test set should still be present");
        } else {
            if (peers[0].clientIp == trackerRegistration.peer.clientIp + 1 &&
                    peers[0].listeningPort == trackerRegistration.peer.listeningPort) {
                System.out.println("Success: Found 'Test4' file from the correct host");
            } else {
                System.out.println("Error: Found 'Test4' file but from incorrect host");
            }
        }
        peers = trackerRegistrationImport.Search("Set1");
        if (peers != null && peers.length == 0) {
            System.out.println("Error: 'Set' test set should still be present");
        } else {
            if (peers[0].clientIp == trackerRegistration2.peer.clientIp + 2 &&
                    peers[0].listeningPort == trackerRegistration2.peer.listeningPort) {
                System.out.println("Success: Found 'Set1' file from the correct host");
            } else {
                System.out.println("Error: Found 'Set1' file but from incorrect host");
            }
        }

        trackerRegistration.AddFilesFromDirectory(ComponentTester.TEST_FILE_PATH_ROOT + "SetTestFileSet");
        trackerRegistration.AddFilesFromDirectory(ComponentTester.TEST_FILE_PATH_ROOT + "EmptyTestFileSet");
        trackerRegistration.AddFilesFromDirectory(ComponentTester.TEST_FILE_PATH_ROOT + "DirectoryDoesntExist");
    }
    
    private static void TestTrackerQueryImportExport() {
        String filename = "Testfilename.ext";
        int listeningPort = 44235;
        TrackerQuery trackerQuery = new TrackerQuery(filename, listeningPort);
        byte[] messageData = trackerQuery.ExportQuery();

        System.out.println("Filename = " + trackerQuery.filename +
                ", listeningPort = " + trackerQuery.listeningPort +
                ", messageData (" + messageData.length + ")");

        TrackerQuery trackerQueryImport = new TrackerQuery();

        if (trackerQueryImport.ImportQuery(messageData)) {
            System.out.println("Import reported success: Filename = " + trackerQueryImport.filename +
                    ", listeningPort = " + trackerQueryImport.listeningPort);
        } else {
            System.out.println("Import reported failure");
        }
    }

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
            peerList[i] = new Peer(currentIP, currentPort);
            currentIP += 3;
            currentPort += 127;
        }

        trackerQueryResponse = new TrackerQueryResponse(peerList);
        messageData = trackerQueryResponse.ExportResponse();

        trackerQueryResponseImport = new TrackerQueryResponse();
        if (trackerQueryResponseImport.ImportResponse(messageData)) {
            System.out.println("TrackerQueryReponse:Import reported success");

            i = 0;
            for (Peer peer : trackerQueryResponseImport.peerList) {
                byte[] bytes = Util.IntToByteArray(peer.clientIp);
                IP = InetAddress.getByAddress(bytes);
                System.out.println(i + ": IP = " + IP + "; Port = " + peer.listeningPort);
                i++;
            }
        } else {
            System.out.println("TrackerQueryReponse:Import failed");
        }

    }

    private static void TestUtilExtractNullTerminatedString() {
        String test2 = "Firstteststring" + '\0' + "Secondteststring" + '\0' + "Thirdteststring";
        byte[] test2bytes = test2.getBytes();
        int currentIndex = 0;
        int[] nextIndex = {0};
        String string;
        int i;

        for (i = 0; i < 3; i++) {
            if ((string = Util.ExtractNullTerminatedString(test2bytes, currentIndex, nextIndex)) != null) {
                System.out.println("Iteration " + i + ": String = " + string +
                        "; currentIndex = " + currentIndex + "; nextIndex = " + nextIndex[0]);
                currentIndex = nextIndex[0];
            } else {
                System.out.println("Iteration " + i + ": NULL");
            }
        }
    }

    private static void TestChunkListRequestImportExport() {
        String filename = "Testfilename.ext";
        int listeningPort = 44234;
        ChunkListRequest request = new ChunkListRequest(filename, listeningPort);
        byte[] messageData = request.ExportMessagePayload();

        System.out.println("Filename = " + request.filename +
                ", listeningPort = " + request.receivingPort +
                ", messageData (" + messageData.length + ")");
        ChunkListRequest requestImport = new ChunkListRequest();

        requestImport.ImportMessagePayload(messageData);

        System.out.println("Filename = " + requestImport.filename +
                ", listeningPort = " + requestImport.receivingPort +
                ", messageData (" + messageData.length + ")");
    }

    private static void TestChunkListResponseImportExport() {
        String filename = "Testfilename.ext";
        ChunkInfo[] chunkInfoList = new ChunkInfo[5];
        byte[] chunkHash = {0x00, 0x00, 0x00, 0x01};
        for ( int i = 0; i < 5; i++ ) {
            chunkInfoList[i] = new ChunkInfo(i, chunkHash, 0);
        }
        ChunkListResponse response = new ChunkListResponse(filename, chunkInfoList);
        byte[] messageData = response.ExportMessagePayload();

        System.out.println("Filename = " + response.filename);
        for (int i = 0; i < response.chunkList.length; i++) {
            System.out.println("chunk[" + i + "]");
        }
        ChunkListResponse responseImport = new ChunkListResponse();

        responseImport.ImportMessagePayload(messageData);

        System.out.println("Filename = " + response.filename);
        for (int i = 0; i < response.chunkList.length; i++) {
            System.out.println("chunk[" + i + "]");
        }
    }

    private static void TestChunkRequestImportExport() {
        String filename = "TestFilename.ext";
        int chunkNumber = 1;
        int listeningPort = 54321;
        ChunkRequest request = new ChunkRequest(filename, chunkNumber, listeningPort);
        byte[] messageData = request.ExportMessagePayload();

        System.out.println("Filename = " + request.filename +
                ", listeningPort = " + request.listeningPort +
                ", messageData (" + messageData.length + ")");

        ChunkRequest requestImport = new ChunkRequest();
        requestImport.ImportMessagePayload(messageData);

        System.out.println("Filename = " + requestImport.filename +
                ", listeningPort = " + requestImport.listeningPort +
                ", messageData (" + messageData.length + ")");

    }

    private static void TestChunkResponseImportExport() {
        String filename = "TestFilename.ext";
        int chunkNumber = 1;
        int listeningPort = 54321;
        byte[] chunkData = {0, 1, 2, 3, 4, 5};
        ChunkResponse response = new ChunkResponse(filename, chunkNumber, chunkData);
        byte[] messageData = response.ExportMessagePayload();

        System.out.println("Filename = " + response.filename +
                ", chunkNumber = " + response.chunkNumber +
                ", chunkData (" + response.chunkData.length + ")" +
                ", messageData (" + messageData.length + ")");

        ChunkResponse responseImport = new ChunkResponse();
        responseImport.ImportMessagePayload(messageData);

        System.out.println("Filename = " + responseImport.filename +
                ", chunkNumber = " + responseImport.chunkNumber +
                ", chunkData (" + responseImport.chunkData.length + ")" +
                ", messageData (" + messageData.length + ")");

        
    }
}

