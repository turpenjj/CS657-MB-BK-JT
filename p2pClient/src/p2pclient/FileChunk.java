/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 *
 * @author Matt
 */
public class FileChunk {
    String filename;
    int chunkNumber;
    byte[] chunk;
    static byte[] sha1Hash = new byte[40];

    FileChunk(String file, int chNumber) {
        filename = file;
        chunkNumber = chNumber;
    }

//    public void SetOffset(int offset) {
//        offsetIntoFile = offset;
//    }
//    public int GetOffset() {
//        return offsetIntoFile;
//    }
//
//    public void SetLength(int length) {
//        chunkLength = length;
//    }
//
//    public int GetLength() {
//        return chunkLength;
//    }

    public void SetChunk(byte[] data, int length) {
        chunk = new byte[length];
        System.arraycopy(data, 0, chunk, 0, length);
        CalcSha1Hash();
    }

    public byte[] GetHash() {
        return sha1Hash;
    }

    public String GetHashString() {
        return ConvertToHex(sha1Hash);
    }

    private void CalcSha1Hash() {
        sha1Hash = SHA1(chunk);
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

    private static byte[] SHA1 (byte[] toHash) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");

            md.update(toHash, 0, toHash.length);
            sha1Hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Trouble, no SHA1 algorithm");
        }
        return sha1Hash;
    }
}
