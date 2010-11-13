/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.net.*;

/**
 *
 * @author Matt
 */
public class FileSender extends Util {
    static RequestingClient myReq;
    public static void main(String args[]) throws Exception
    {
        int peerIp = ByteArrayToInt(InetAddress.getByName("192.168.1.102").getAddress());

        Peer myPeer = new Peer("Test", peerIp, 9876);
        myReq = new RequestingClient(myPeer, "devlist.txt");

        myReq.start();
        try {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter File to send: ");
            String sentence = inFromUser.readLine();
            SendFile(sentence);
        } catch (UnknownHostException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    private static void SendFile(String filename) {
        File file = new File(filename);
        FileChunk currentChunk = new FileChunk(filename, 0);
        
        try {
            FileInputStream file_input = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int fileOffset = 0;
            int len;

            String serverHostname = new String ("127.0.0.1");
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(serverHostname);


            while ( (len = file_input.read(buf)) > 0 ) {
                int lengthToSend = len + 28;
                byte[] sendBuffer = new byte[lengthToSend];

//                currentChunk.SetOffset(fileOffset);
//                currentChunk.SetLength(len);
                currentChunk.SetChunk(buf, len);
                System.out.println("Sending chunk with hash: " + currentChunk.GetHashString() + "and length: " + lengthToSend);
//                System.arraycopy(IntToByteArray(currentChunk.GetOffset()), 0, sendBuffer, 0, 4);
//                System.arraycopy(IntToByteArray(currentChunk.GetLength()), 0, sendBuffer, 4, 4);
                System.arraycopy(currentChunk.GetHash(), 0, sendBuffer, 8, currentChunk.GetHash().length);
//                System.arraycopy(currentChunk.chunk, 0, sendBuffer, 28, currentChunk.GetLength());

                DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, lengthToSend, IPAddress, 9876);

                clientSocket.send(sendPacket);
                fileOffset += len;
            }
            clientSocket.close();
        } catch ( IOException e ) {
            System.out.println("Exception: " + e);
        }
    }
}
