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
public class FileSender {
    public static void main(String args[]) throws Exception
    {
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
        FileChunk currentChunk = new FileChunk();
        
        try {
            FileInputStream file_input = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int fileOffset = 0;
            int len;

            String serverHostname = new String ("127.0.0.1");
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(serverHostname);


            while ( (len = file_input.read(buf)) > 0 ) {
                currentChunk.SetOffset(fileOffset);
                currentChunk.SetLength(len);
                currentChunk.SetChunk(buf, len);

                System.out.println("Sending chunk with hash: " + currentChunk.GetHash());
                DatagramPacket sendPacket =
                    new DatagramPacket(buf, len, IPAddress, 9876);

                clientSocket.send(sendPacket);
            }
            clientSocket.close();
        } catch ( IOException e ) {
            System.out.println("Exception: " + e);
        }
    }
}
