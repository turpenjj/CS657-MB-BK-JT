package p2pclient;

import java.io.*;

/**
 *
 * @author Jeremy
 */
public class RealFileFilter implements java.io.FileFilter {
    RealFileFilter() {
        super();
    }

    public boolean accept(File f) {
        if (f == null) {
            return false;
        }

        if (f.isFile() && f.canRead()) {
            return true;
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "Real files (not directories)";
    }
}
