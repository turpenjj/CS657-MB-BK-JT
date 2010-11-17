/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.net.*;

/**
 * Early test class for performing socket read/write operations. Implemented a
 * simple file sender that sent file data packets out to FileReceiver.
 *
 * @see FileReceiver
 *
 * @author Matt
 */
public class FileSender extends Util {
    static RequestingClient myReq;
    static RequestingClient myReq1;
    public static void main(String args[]) throws Exception
    {
        Host myHost;
        try {
            myHost = new Host(5, "dir", InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            Util.DebugPrint(DbgSub.FILE_SENDER, "Caught exception " + e);
            return;
        }
        myHost.start();
//        int peerIp = ByteArrayToInt(InetAddress.getByName("192.168.1.100").getAddress());
        Peer myPeer = new Peer(InetAddress.getByName("192.168.1.100"), 9876);
//        myReq = new RequestingClient(myPeer, "devlist.txt");
//        myReq1 = new RequestingClient(myPeer, "devlis1t.txt");

        myReq.start();
//        myReq1.start();
//        myReq.RequestChunkList();
        String receivedFilename = null;
        int receivedChunkList[] = null;
//        myReq1.RequestChunkList("devlist.txt");
//        myReq.RequestChunk(1);
//        try {
//            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//            System.out.print("Enter File to send: ");
//            String sentence = inFromUser.readLine();
//            SendFile(sentence);
//        } catch (UnknownHostException ex) {
//            System.err.println(ex);
//        } catch (IOException ex) {
//            System.err.println(ex);
//        }
    }
    
//    private static void SendFile(String filename) {
//        File file = new File(filename);
//        FileChunk currentChunk = new FileChunk(filename, 0);
//
//        try {
//            FileInputStream file_input = new FileInputStream(file);
//            byte[] buf = new byte[1024];
//            int fileOffset = 0;
//            int len;
//
//            String serverHostname = new String ("127.0.0.1");
//            DatagramSocket clientSocket = new DatagramSocket();
//            InetAddress IPAddress = InetAddress.getByName(serverHostname);
//
//
//            while ( (len = file_input.read(buf)) > 0 ) {
//                int lengthToSend = len + 28;
//                byte[] sendBuffer = new byte[lengthToSend];
//
////                currentChunk.SetOffset(fileOffset);
////                currentChunk.SetLength(len);
//                currentChunk.SetChunk(buf, len);
//                Util.DebugPrint(DbgSub.FILE_SENDER, "Sending chunk with hash: " + currentChunk.GetHashString() + "and length: " + lengthToSend);
////                System.arraycopy(IntToByteArray(currentChunk.GetOffset()), 0, sendBuffer, 0, 4);
////                System.arraycopy(IntToByteArray(currentChunk.GetLength()), 0, sendBuffer, 4, 4);
//                System.arraycopy(currentChunk.GetHash(), 0, sendBuffer, 8, currentChunk.GetHash().length);
////                System.arraycopy(currentChunk.chunk, 0, sendBuffer, 28, currentChunk.GetLength());
//
//                DatagramPacket sendPacket =
//                    new DatagramPacket(sendBuffer, lengthToSend, IPAddress, 9876);
//
//                clientSocket.send(sendPacket);
//                fileOffset += len;
//            }
//            clientSocket.close();
//        } catch ( IOException e ) {
//            Util.DebugPrint(DbgSub.FILE_SENDER, "Exception: " + e);
//        }
//    }
}
