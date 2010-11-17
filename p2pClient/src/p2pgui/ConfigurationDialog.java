/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfigurationDialog.java
 *
 * Created on Nov 12, 2010, 12:19:02 PM
 */

package p2pgui;

import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author Brett
 */
public class ConfigurationDialog extends javax.swing.JFrame {

    private DirectorySelect ds;
    private File    torrentDir;
    private int     maxDownloads;
    private int     maxUploads;
    private int     portNumber;
    private boolean isTracker;
    private String  TrackerIp;
    private boolean hostConfigured;

    /** Creates new form ConfigurationDialog */
    public ConfigurationDialog() {
        ds = new DirectorySelect();
        torrentDir = null;
        maxDownloads = 0;
        maxUploads = 0;
        portNumber = 0;
        hostConfigured = false;
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        Directory = new javax.swing.JLabel();
        DirectoryEntry = new javax.swing.JTextField();
        BrowseButton = new javax.swing.JButton();
        PortNumber = new javax.swing.JLabel();
        ConcurrentDL = new javax.swing.JLabel();
        ConcurrentUL = new javax.swing.JLabel();
        PortNumberEntry = new javax.swing.JTextField();
        MaxDownloadSelect = new javax.swing.JSlider();
        MaxUploadSelect = new javax.swing.JSlider();
        YesButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        TrackerIPField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        NoButton = new javax.swing.JRadioButton();

        OKButton.setText("OK");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration"));

        Directory.setFont(new java.awt.Font("Tahoma", 1, 11));
        Directory.setText("Torrent File Directory:");
        Directory.setToolTipText("Specify the directory containing your torrent files.");

        BrowseButton.setText("Browse...");
        BrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonActionPerformed(evt);
            }
        });

        PortNumber.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        PortNumber.setText("Port Number:");
        PortNumber.setToolTipText("Select the port number to to use for downloads/uploads");

        ConcurrentDL.setFont(new java.awt.Font("Tahoma", 1, 11));
        ConcurrentDL.setText("Maximum concurrent Downloads:");

        ConcurrentUL.setFont(new java.awt.Font("Tahoma", 1, 11));
        ConcurrentUL.setText("Maximum concurrent Uploads:");

        MaxDownloadSelect.setMajorTickSpacing(10);
        MaxDownloadSelect.setMaximum(50);
        MaxDownloadSelect.setMinorTickSpacing(5);
        MaxDownloadSelect.setPaintLabels(true);
        MaxDownloadSelect.setPaintTicks(true);
        MaxDownloadSelect.setSnapToTicks(true);
        MaxDownloadSelect.setValue(0);

        MaxUploadSelect.setMajorTickSpacing(10);
        MaxUploadSelect.setMaximum(50);
        MaxUploadSelect.setMinorTickSpacing(5);
        MaxUploadSelect.setPaintLabels(true);
        MaxUploadSelect.setPaintTicks(true);
        MaxUploadSelect.setSnapToTicks(true);
        MaxUploadSelect.setValue(0);

        buttonGroup1.add(YesButton);
        YesButton.setText("Yes");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Tracker:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Tracker IP:");

        buttonGroup1.add(NoButton);
        NoButton.setSelected(true);
        NoButton.setText("No");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(Directory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(DirectoryEntry, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(BrowseButton)
                        .addContainerGap(72, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(ConcurrentDL)
                                .addGap(18, 18, 18))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(ConcurrentUL)
                                .addGap(18, 18, 18)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(MaxUploadSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MaxDownloadSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(200, 200, 200))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(123, 123, 123)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(PortNumber)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(YesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(NoButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PortNumberEntry, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                            .addComponent(TrackerIPField, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))))
                .addGap(318, 318, 318))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Directory, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DirectoryEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BrowseButton))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(MaxDownloadSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(ConcurrentDL)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(MaxUploadSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(ConcurrentUL)))
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(YesButton)
                    .addComponent(NoButton))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(TrackerIPField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PortNumber)
                    .addComponent(PortNumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(45, 45, 45))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(OKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CancelButton)
                    .addComponent(OKButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void BrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonActionPerformed
        boolean result = ds.displayDirectorySelect();
        
        if (result == true) {
            File dir = ds.getTorrentDir();
            DirectoryEntry.setText(dir.toString());
        } else if (result == false) {
            ds.setVisible(false);
        }
    }//GEN-LAST:event_BrowseButtonActionPerformed

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        // if direntry is null or port is null show error dialog
        if ( DirectoryEntry.getText().length() == 0 ) {
        JOptionPane.showMessageDialog(ds,
                "Please select a Torrent file directory",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } else if ( (MaxDownloadSelect.getValue() == 0) || (MaxUploadSelect.getValue() == 0) ) {
            JOptionPane.showMessageDialog(ds,
                "Maximum Downloads and Maximum Uploads must be non-zero",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } else if ( PortNumberEntry.getText().length() == 0 ) {
            JOptionPane.showMessageDialog(ds,
                "Please select a port number",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } else if ( NoButton.isSelected() && (TrackerIPField.getText().length() == 0) ) {
            JOptionPane.showMessageDialog(ds,
                "Please specify a Tracker IP Address",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } else {
            torrentDir = new File(DirectoryEntry.getText());
            portNumber = ((Number)(Integer.parseInt(PortNumberEntry.getText()))).intValue();
            TrackerIp = (TrackerIPField.getText());
            maxDownloads = MaxDownloadSelect.getValue();
            maxUploads = MaxUploadSelect.getValue();
            this.setVisible(false);
            GUI.getInstance().updateHostedFiles(torrentDir);

            if (YesButton.isSelected()) {
                GUI.getInstance().InitializeHost(torrentDir, portNumber, TrackerIp, true);
            } else {
                GUI.getInstance().InitializeHost(torrentDir, portNumber, TrackerIp, false);
            }
        }
    }//GEN-LAST:event_OKButtonActionPerformed

    File GetTorrentDir() {
        return torrentDir;
    }
    /**
    * @param args the command line arguments
    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                this.setLocationRelativeTo(null);
//                this.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BrowseButton;
    private javax.swing.JButton CancelButton;
    private javax.swing.JLabel ConcurrentDL;
    private javax.swing.JLabel ConcurrentUL;
    private javax.swing.JLabel Directory;
    private javax.swing.JTextField DirectoryEntry;
    private javax.swing.JSlider MaxDownloadSelect;
    private javax.swing.JSlider MaxUploadSelect;
    private javax.swing.JRadioButton NoButton;
    private javax.swing.JButton OKButton;
    private javax.swing.JLabel PortNumber;
    private javax.swing.JTextField PortNumberEntry;
    private javax.swing.JTextField TrackerIPField;
    private javax.swing.JRadioButton YesButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
