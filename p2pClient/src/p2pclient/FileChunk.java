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
    static byte[] sha1Hash = new byte[40];

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

    public byte[] GetHash() {
        return sha1Hash;
    }

    public String GetHashString() {
        return ConvertToHex(sha1Hash);
    }

    private void CalcSha1Hash() {
        sha1Hash = SHA1(chunk);
    }

    public byte[] IntToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)(value)};
    }

    public int ByteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
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
