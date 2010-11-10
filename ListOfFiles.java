/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package p2pclient;

import java.io.*;
import java.util.*;

/**
 *
 * @author Matt
 */
public class ListOfFiles implements Enumeration {

    private String[] listOfFiles;
    private int current = 0;

    public ListOfFiles(String[] listOfFiles) {
        this.listOfFiles = listOfFiles;
    }

    public boolean hasMoreElements() {
        if (current < listOfFiles.length)
            return true;
        else
            return false;
    }

    public Object nextElement() {
        InputStream in = null;

        if (!hasMoreElements())
            throw new NoSuchElementException("No more files.");
        else {
            String nextElement = listOfFiles[current];
            current++;
            try {
                in = new FileInputStream(nextElement);
            } catch (FileNotFoundException e) {
                System.err.println("ListOfFiles: Can't open " + nextElement);
            }
        }
        return in;
    }
}
