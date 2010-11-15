/*
 * DirectorySelect.java
 *
 * Created on Nov 12, 2010, 8:14:15 PM
 */

package p2pgui;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Brett
 */
public class DirectorySelect extends javax.swing.JFrame {

    private File torrentDir;

    /** Creates new form DirectorySelect */
    public DirectorySelect() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();

        setAlwaysOnTop(true);

        jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        jFileChooser1.setSelectedFile(new java.io.File("C:\\"));

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    public File getTorrentDir()
    {
        return torrentDir;
    }

    public boolean displayDirectorySelect() {
        int result = jFileChooser1.showOpenDialog(this);
        switch (result) {
            case JFileChooser.APPROVE_OPTION:
                // Approve (Open or Save) was clicked
                torrentDir = new File(jFileChooser1.getSelectedFile().toString());
                return true;
            case JFileChooser.CANCEL_OPTION:
                return false;
            case JFileChooser.ERROR_OPTION:
                return false;
        }
        return false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooser1;
    // End of variables declaration//GEN-END:variables

}
