/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 *
 * @author Matt
 */
public class Util {
    public static void DebugPrint(DbgSub subsys, Object object) {
        if (ComponentTesterConfig.DebugLevels[subsys.ordinal()]) {
            System.out.println(subsys + ": " + object);
        }
    }
    
    public static int GetRandomHighPort(Random rand) {
        return (rand.nextInt(65535 - 49152) + 49152);
    }

    public static String ConvertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();

        for ( int i = 0; i < data.length; i++ ) {
            int halfbyte =  (data[i] >>> 4) & 0x0F;
            int two_halves = 0;
            do {
                if ( (0 <= halfbyte) && (halfbyte <= 9) ) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halves++ < 1);
        }
        return buf.toString();
    }

    public static byte[] IntToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)(value)};
    }

    public static int IntToByteArray(byte[] b, int index, int value) {
        b[index + 0] = (byte)(value >>> 24);
        b[index + 1] = (byte)(value >>> 16);
        b[index + 2] = (byte)(value >>> 8);
        b[index + 3] = (byte)(value >>> 0);

        return index + 4;
    }

    public static int IntArrayToByteArray(byte[] b, int index, int[] value) {
        if ( value == null ) {
            return index;
        }
        for ( int i = 0; i < value.length; i++ ) {
            IntToByteArray(b, index + i*4, value[i]);
        }
        return index + 4*value.length;
    }

    public static int ByteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public static int ByteArrayToInt(byte[] b, int startingIndex) {
        return (((b[startingIndex + 0] & 0xFF) << 24) +
                ((b[startingIndex + 1] & 0xFF) << 16) +
                ((b[startingIndex + 2] & 0xFF) << 8) +
                ((b[startingIndex + 3] & 0xFF) << 0));
    }

    public static int InetAddressToInt(InetAddress addr) {
        if (addr == null) {
            return 0;
        }

        byte[] bytes = addr.getAddress();

        if (bytes == null) {
            return 0;
        }

        return Util.ByteArrayToInt(bytes);
    }

    /**
     *
     * @param b Byte array to extract a null-terminated ASCII string from
     * @param startingIndex Index into byte array to start at
     * @param[out] nextIndex Index into byte array for the first byte following
     *      the string's null-terminator (single element array)
     * @return Extracted string, null on failure
     */
    public static String ExtractNullTerminatedString(byte[] b, int startingIndex, int[] nextIndex) {
        String string = null;
        int i;

        if (b.length < 1 || startingIndex < 0) {
            return null;
        }

        for (i = startingIndex; i < b.length; i++) {
            if (b[i] == '\0') {
                break;
            }
        }

        if (i == b.length) {
            return null;
        }

        try {
           string = new String(b, startingIndex, i - startingIndex, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            Util.DebugPrint(DbgSub.UTIL, "Received encoding exception " + e);
            return null;
        }
        // move past the null-terminator
        i++;
        if ( nextIndex != null ) {
            nextIndex[0] = i;
        }

        return string;
    }

    public static int InsertNullTerminatedString(byte[] b, int startingIndex, String string) {
        byte[] s;

        try {
           s = string.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            Util.DebugPrint(DbgSub.UTIL, "Received encoding exception " + e);
            return startingIndex;
        }

        System.arraycopy(s, 0, b, startingIndex, s.length);
        b[startingIndex + s.length] = '\0';

        return startingIndex + s.length + 1;
    }

    public static int[] ByteArrayToIntArray(byte[] b) {
        if (b.length % 4 != 0) {
            return null;
        }
        int[] intArray = new int[b.length/4];

        for ( int i = 0; i < b.length/4; i++ ) {
            int byteArrayIndex = i*4;
            intArray[i] = (b[byteArrayIndex] << 24)
                    + ((b[byteArrayIndex+1] & 0xFF) << 16)
                    + ((b[byteArrayIndex+2] & 0xFF) << 8)
                    + (b[byteArrayIndex+3] & 0xFF);
        }

        return intArray;
    }

//    public static boolean SendPacket(Peer peer, byte[] payload) {
//        int SOCKET_TIMEOUT = 15000;
//        try {
//           DatagramSocket sendingSocket = new DatagramSocket();
//           InetAddress IPAddress = peer.clientIp;
//           int port = peer.listeningPort;
//
//           DatagramPacket packetToSend = new DatagramPacket(payload, payload.length, IPAddress, port);
//
//           sendingSocket.setSoTimeout(SOCKET_TIMEOUT);
//           sendingSocket.send(packetToSend);
//           sendingSocket.close();
//        } catch ( IOException e ) {
//            Util.DebugPrint(DbgSub.UTIL, "SendPacket Error: " + e);
//            return false;
//        }
//        return true;
//    }
//
//    public static int ReceivePacket(Peer peer, int receivingPort, byte[] receivedData) {
//        int SOCKET_TIMEOUT = 15000;
//        int lengthReceived = 0;
//        int MAX_PACKET_SIZE = 1500;
//        receivedData = new byte[MAX_PACKET_SIZE];
//
//        try {
//            DatagramSocket receivingSocket = new DatagramSocket(receivingPort);
//            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
//
//            receivingSocket.setSoTimeout(SOCKET_TIMEOUT);
//            receivingSocket.receive(receivedPacket);
//            InetAddress senderIP = receivedPacket.getAddress();
//
//            peer.clientIp = senderIP;
//            lengthReceived = receivedPacket.getLength();
//        } catch ( IOException e ) {
//            Util.DebugPrint(DbgSub.UTIL, "ReceivePacket Error: " + e);
//        }
//        return lengthReceived;
//    }

//    public static int ReceiveCommunication(Peer peer, int receivingPort, byte[] receivedData) {
//        int bytesRead;
//        int totalBytesReceived = 0;
//        int sessionID = -1;
//        byte[] rawReceivedData;
//        byte[] tempArray = new byte[16];
//        int[] rawPacketHeader;
//        PacketHeader packetHeader;
//
//        do {
//            rawReceivedData = null;
//            bytesRead = ReceivePacket(peer, receivingPort, rawReceivedData);
//            if ( bytesRead < 16 ) {
//                return 0;
//            }
//            System.arraycopy(rawReceivedData, 0, tempArray, 0, 16);
//            rawPacketHeader = ByteArrayToIntArray(tempArray);
//            packetHeader = new PacketHeader(rawPacketHeader[0], rawPacketHeader[1], rawPacketHeader[2], rawPacketHeader[3]);
//
//            if ( sessionID == -1 ) {
//                sessionID = packetHeader.sessionID;
//                receivedData = new byte[packetHeader.totalSize];
//            } else if ( sessionID != packetHeader.sessionID ) {
//                Util.DebugPrint(DbgSub.UTIL, "Received packet for the wrong session");
//                return 0;
//            }
//            System.arraycopy(rawReceivedData, 16, receivedData, packetHeader.offset, bytesRead - 16);
//            totalBytesReceived += bytesRead - 16;
//        } while ( totalBytesReceived < packetHeader.totalSize ) ;
//        return totalBytesReceived;
//    }


    public static long GetCurrentTime() {
        return (Calendar.getInstance()).getTimeInMillis();
    }

    public static byte[] ReadFileData(String path, String filename) {
        File file = new File(path + File.separator + filename);
        FileInputStream fileInputStream;
        long totalLength;
        byte[] fileData;
        int i;
        int bytesRead;
        int currentIndex;

        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.UTIL, "Caught exception " + e);

            return null;
        }
        totalLength = file.length();
        fileData = new byte[(int)totalLength];

        currentIndex = 0;
        bytesRead = 1;
        while (currentIndex < fileData.length && bytesRead > 0) {
            try {
                bytesRead = fileInputStream.read(fileData, currentIndex, fileData.length - currentIndex);
            } catch (Exception e) {
                Util.DebugPrint(DbgSub.UTIL, "Caught exception " + e);

                return null;
            }

            if (bytesRead > 0) {
                currentIndex += bytesRead;
            }
        }

        return fileData;
    }

    public static boolean WriteFileData(String path, String filename, byte[] fileData) {
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(path + File.separator + filename);

            outputStream.write(fileData);

            outputStream.close();
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.CHUNK_MANAGER, "Caught exception " + e);

            return false;
        }

        return true;
    }
}