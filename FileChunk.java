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
    int offsetIntoFile;
    int chunkLength;
    byte[] chunk;
    String sha1Hash;

    public void SetOffset(int offset) {
        offsetIntoFile = offset;
    }
    public int GetOffset() {
        return offsetIntoFile;
    }

    public void SetLength(int length) {
        chunkLength = length;
    }

    public int GetLength() {
        return chunkLength;
    }

    public void SetChunk(byte[] data, int length) {
        chunk = new byte[length];
        System.arraycopy(data, 0, chunk, 0, length);
        CalcSha1Hash();
    }

    public String GetHash() {
        return sha1Hash.toString();
    }

    private void CalcSha1Hash() {
        sha1Hash = SHA1(chunk); //"abcdabed12436812bc081234567890abcdef6542";
    }

    private static String ConvertToHex(byte[] data) {
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

    private static String SHA1 (byte[] toHash) {
        byte[] sha1hash = new byte[40];

        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");

            md.update(toHash, 0, toHash.length);
            sha1hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Trouble, no SHA1 algorithm");
        }
        return ConvertToHex(sha1hash);
    }
}
