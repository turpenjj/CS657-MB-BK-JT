/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
        
        try {
            FileInputStream file_input = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len;

            String serverHostname = new String ("127.0.0.1");
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(serverHostname);

            while ( (len = file_input.read(buf)) > 0 ) {

                System.out.printf("Read: %s", buf.toString());
                DatagramPacket sendPacket =
                    new DatagramPacket(buf, buf.length, IPAddress, 9876);

                clientSocket.send(sendPacket);
            }
            clientSocket.close();
        } catch ( IOException e ) {
            System.out.println("Exception: " + e);
        }
    }
}
