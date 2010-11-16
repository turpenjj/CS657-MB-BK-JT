/*
 * GUI.java
 *
 * Created on Nov 12, 2010, 12:11:53 PM
 */

package p2pgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import p2pclient.ChunkInfo;
import p2pclient.Host;
import p2pclient.Peer;
import p2pclient.RegisteredPeer;
import p2pclient.Torrent;
import p2pclient.Tracker;
import java.util.logging.Logger;
/**
 *
 * @author Brett
 */
public class GUI extends javax.swing.JFrame {

    private static ConfigurationDialog config;
    private static GUI gui;
    private Host host;
    private Tracker tracker;
    private String searchFileName;
    private Peer[] hostingPeers;

    private GUI()
    {
        host = null;
        tracker = null;
        config = new ConfigurationDialog();
        initComponents();

        DownloadFileList.addListSelectionListener(new DownloadFileListListener());
        UploadFileList.addListSelectionListener(new UploadFileListListener());
        RegisteredPeersList.addListSelectionListener(new RegisteredPeersListListener());

        // Initialize the timer to update download/upload progress
        int fire = 2000; //milliseconds
        timer = new Timer(fire, TimerUpdate);
        timer.setInitialDelay(5000);
        timer.start();

    }

    /*
     * Description:
     *   Returns single instance of the GUI class
     */
    public static GUI getInstance() {
        if ( gui != null ) {
            return gui;
        }

        GUI newGUI = new GUI();
        return newGUI;
    }

    /*
     * Description:
     *   This Listener updates the downloaded file and chunk list whenever
     *   a new file is selected in the list by the user.
     *
     *   It also updates the chunk status and the total download progress for the file
     */
    class DownloadFileListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                String file = (String) DownloadFileList.getSelectedValue();

                if (file != null) {
                    ChunkInfo[] chunks = host.GetFileChunkInfo(file);

                    float totalAvailable = 0;
                    DefaultTableModel model = (DefaultTableModel)DownloadChunks.getModel();
                    model.setRowCount(0);
                    for (int i = 0; i < chunks.length; i++) {
                        String state = null;
                        switch(chunks[i].status) {
                            case 0:
                                state = "Missing";
                                break;
                            case 1:
                                state = "Download in Progress";
                                break;
                            case 2:
                                state = "Available";
                                totalAvailable++;
                                break;
                            default:
                                break;
                        }
                        model.addRow(new Object[]{chunks[i].chunkNumber, state, chunks[i].receivedFrom.clientIp});
                    }
                    DownloadChunks.setModel(model);

                    int percentComplete = (int) ((totalAvailable/chunks.length) * 100);
                    FileDownloadProgress.setValue(percentComplete);
                    FileDownloadProgress.setString(String.valueOf(percentComplete) + "%");
                    FileDownloadProgress.setStringPainted(true);
                }
            }
        }
    }

    /*
     * Description:
     *   This Listener updates the uploaded file and chunk list whenever
     *   a new file is selected in the list by the user.
     *
     *   It also updates the chunk status and the total download progress for the file
     */
    class UploadFileListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                String file = (String) UploadFileList.getSelectedValue();

                if (file != null) {
                    ChunkInfo[] chunks = host.GetFileChunkInfo(file);

                    float totalAvailable = 0;
                    DefaultTableModel model = (DefaultTableModel)UploadChunks.getModel();
                    model.setRowCount(0);
                    for (int i = 0; i < chunks.length; i++) {
                        String state = null;
                        switch(chunks[i].status) {
                            case 0:
                                state = "Missing";
                                break;
                            case 1:
                                state = "Download in Progress";
                                break;
                            case 2:
                                state = "Available";
                                totalAvailable++;
                                break;
                            default:
                                break;
                        }
                        model.addRow(new Object[]{chunks[i].chunkNumber, state, chunks[i].receivedFrom.clientIp});
                    }
                    UploadChunks.setModel(model);

                    int percentComplete = (int) ((totalAvailable/chunks.length) * 100);
                    FileUploadProgress.setValue(percentComplete);
                    FileUploadProgress.setString(String.valueOf(percentComplete) + "%");
                    FileUploadProgress.setStringPainted(true);
                }
            }
        }
    }

    /*
     * Description:
     *   This Listener updates the uploaded file and chunk list whenever
     *   a new file is selected in the list by the user.
     *
     *   It also updates the chunk status and the total download progress for the file
     */
    class RegisteredPeersListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                String peer = (String) RegisteredPeersList.getSelectedValue();
                
                if ( peer != null ) {
                    RegisteredPeer[] peers = tracker.registeredPeers.registeredPeers;
                    for (int i = 0; i < peers.length; i++) {
                        if ( peers[i].peer.clientIp.toString().equals(peer) ) {
                            String[] files = peers[i].files;
                            DefaultTableModel model = (DefaultTableModel) RegisteredPeersTable.getModel();
                            model.setRowCount(0);
                            for (i = 0; i < files.length; i++) {
                                model.addRow(new Object[]{files[i]});
                            }
                            RegisteredPeersTable.setModel(model);
                            break;
                        }
                    }

                }
            }
        }
    }

    /*
     * Description:
     *   Initializes the Host instance.  The GUI uses the host to get information
     *   from the outside, to perform search/get search results, and to start downloads
     */
    public void InitializeHost(File torrentDir, int portNumber) {
        if ( host == null ) {
            host = new Host(portNumber, torrentDir.toString());
        } else {
            //host.UpdateConfiguration(torrentDir, portNumber);
        }

        tracker = new Tracker(portNumber);
    }

    /*
     * Description:
     *   Initializes the Host instance.  The GUI uses the host to get information
     *   from the outside, to perform search/get search results, and to start downloads
     */
    public void InitializeHost(File torrentDir, int portNumber, String trackerIp) {
        if ( host == null ) {
            host = new Host(portNumber, torrentDir.toString());
        } else {
            //host.UpdateConfiguration(torrentDir, portNumber);
        }
        // Remove the Tracker Tab
        jTabbedPane1.remove(4);
    }

    /*
     * Description:
     *   This Action Listener updates the downloaded and uploaded file list when
     *   the timer fires.
     */
    ActionListener TimerUpdate = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
            DefaultListModel list;
            int index;
            if (host != null) {
                // Update Downloading files list
                String[] files = host.GetCurrentDownloads();
                index = DownloadFileList.getSelectedIndex();

                list = new DefaultListModel();
                DownloadFileList.removeAll();
                if ( files != null ) {
                    for (int i = 0; i < files.length; i++) {
                        list.addElement(files[i]);
                    }
                }
                DownloadFileList.setModel(list);
                DownloadFileList.setSelectedIndex(index);

                // Now update the Uploading files list
                files = host.GetCurrentUploads();
                index = UploadFileList.getSelectedIndex();

                list = new DefaultListModel();
                UploadFileList.removeAll();
                if ( files != null ) {
                    for (int i = 0; i < files.length; i++) {
                        list.addElement(files[i]);
                    }
                }
                UploadFileList.setModel(list);
                UploadFileList.setSelectedIndex(index);

            }

            if (tracker != null) {
                // Update the Registered Peers list
                RegisteredPeer[] peers = tracker.registeredPeers.registeredPeers;
                index = RegisteredPeersList.getSelectedIndex();

                if ( peers[0] != null ) {
                    list = new DefaultListModel();
                    RegisteredPeersList.removeAll();
                    for (int i = 0; i < peers.length; i++) {
                        list.addElement(peers[i].peer.clientIp.toString());
                    }
                    RegisteredPeersList.setModel(list);
                    RegisteredPeersList.setSelectedIndex(index);
                }

                // Now update the registered torrents table
                Torrent[] torrents = tracker.registeredTorrents.registeredTorrents;

                if ( torrents != null ) {
                    DefaultTableModel model = (DefaultTableModel) RegisteredTorrentsTable.getModel();
                    model.setRowCount(0);
                    for (int i = 0; i < torrents.length; i++) {
                        model.addRow(new Object[]{torrents[i].filename, torrents[i].filesize, torrents[i].numChunks});
                    }
                    RegisteredTorrentsTable.setModel(model);
                }
            }
        }
    };
    private final Timer timer;


    /*
     * Description:
     *   Updates the hosted file directory whenever the user changes the configuration
     */
    public void updateHostedFiles(File torrentDir) {
        if ( torrentDir.isDirectory() ) {
            HostedFilesList.setText(null); // clear list
             String fileList[] = torrentDir.list();
             for (int i = 0; i < fileList.length; i++) {
                 HostedFilesList.append(fileList[i] + "\n");
             }
        }
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SearchButton = new javax.swing.JButton();
        SearchBox = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        Downloads = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        DownloadChunks = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        DownloadFileList = new javax.swing.JList();
        FileDownloadProgress = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Uploads = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        UploadFileList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        FileUploadProgress = new javax.swing.JProgressBar();
        jScrollPane7 = new javax.swing.JScrollPane();
        UploadChunks = new javax.swing.JTable();
        HostedFiles = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        HostedFilesList = new javax.swing.JTextArea();
        QueryResults = new javax.swing.JPanel();
        DownloadFromPeer = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        SearchResults = new javax.swing.JTable();
        QueryResultsHeader = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        RegisteredPeersList = new javax.swing.JList();
        jScrollPane8 = new javax.swing.JScrollPane();
        RegisteredPeersTable = new javax.swing.JTable();
        jScrollPane9 = new javax.swing.JScrollPane();
        RegisteredTorrentsTable = new javax.swing.JTable();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        Exit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        Config = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("P2P");

        SearchButton.setText("Search");
        SearchButton.setToolTipText("Search for torrent files that are available for download.");
        SearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchButtonActionPerformed(evt);
            }
        });

        SearchBox.setToolTipText("Enter the torrent file name to search for.");

        DownloadChunks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Chunk", "State", "Host"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(DownloadChunks);

        jScrollPane5.setViewportView(DownloadFileList);

        FileDownloadProgress.setStringPainted(true);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setText("File:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel3.setText("Total Progress:");

        javax.swing.GroupLayout DownloadsLayout = new javax.swing.GroupLayout(Downloads);
        Downloads.setLayout(DownloadsLayout);
        DownloadsLayout.setHorizontalGroup(
            DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DownloadsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGroup(DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DownloadsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                            .addComponent(FileDownloadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)))
                    .addGroup(DownloadsLayout.createSequentialGroup()
                        .addGap(281, 281, 281)
                        .addComponent(jLabel3)))
                .addContainerGap())
        );
        DownloadsLayout.setVerticalGroup(
            DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DownloadsLayout.createSequentialGroup()
                .addGroup(DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DownloadsLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel1))
                    .addGroup(DownloadsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(DownloadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(DownloadsLayout.createSequentialGroup()
                        .addComponent(FileDownloadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Downloads               ", Downloads);

        jScrollPane6.setViewportView(UploadFileList);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel2.setText("File:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel4.setText("Total Progress:");

        FileUploadProgress.setStringPainted(true);

        UploadChunks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Chunk", "State", "Host"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(UploadChunks);

        javax.swing.GroupLayout UploadsLayout = new javax.swing.GroupLayout(Uploads);
        Uploads.setLayout(UploadsLayout);
        UploadsLayout.setHorizontalGroup(
            UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 898, Short.MAX_VALUE)
            .addGroup(UploadsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGroup(UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(UploadsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                            .addComponent(FileUploadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)))
                    .addGroup(UploadsLayout.createSequentialGroup()
                        .addGap(281, 281, 281)
                        .addComponent(jLabel4)))
                .addContainerGap())
        );
        UploadsLayout.setVerticalGroup(
            UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 514, Short.MAX_VALUE)
            .addGroup(UploadsLayout.createSequentialGroup()
                .addGroup(UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(UploadsLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel2))
                    .addGroup(UploadsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(UploadsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(UploadsLayout.createSequentialGroup()
                        .addComponent(FileUploadProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Uploads                 ", Uploads);

        HostedFilesList.setColumns(20);
        HostedFilesList.setEditable(false);
        HostedFilesList.setRows(5);
        jScrollPane1.setViewportView(HostedFilesList);

        javax.swing.GroupLayout HostedFilesLayout = new javax.swing.GroupLayout(HostedFiles);
        HostedFiles.setLayout(HostedFilesLayout);
        HostedFilesLayout.setHorizontalGroup(
            HostedFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 898, Short.MAX_VALUE)
        );
        HostedFilesLayout.setVerticalGroup(
            HostedFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Hosted Files            ", HostedFiles);

        DownloadFromPeer.setText("Download from Selected Peer");
        DownloadFromPeer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DownloadFromPeerActionPerformed(evt);
            }
        });

        SearchResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Hosting IP", "Credit"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(SearchResults);

        QueryResultsHeader.setFont(new java.awt.Font("Tahoma", 1, 12));
        QueryResultsHeader.setBorder(null);
        QueryResultsHeader.setOpaque(false);

        javax.swing.GroupLayout QueryResultsLayout = new javax.swing.GroupLayout(QueryResults);
        QueryResults.setLayout(QueryResultsLayout);
        QueryResultsLayout.setHorizontalGroup(
            QueryResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, QueryResultsLayout.createSequentialGroup()
                .addContainerGap(681, Short.MAX_VALUE)
                .addComponent(DownloadFromPeer, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 898, Short.MAX_VALUE)
            .addGroup(QueryResultsLayout.createSequentialGroup()
                .addComponent(QueryResultsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        QueryResultsLayout.setVerticalGroup(
            QueryResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(QueryResultsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(QueryResultsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(DownloadFromPeer)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Query Results              ", QueryResults);

        jSplitPane1.setDividerLocation(125);

        jScrollPane3.setViewportView(RegisteredPeersList);

        jSplitPane1.setLeftComponent(jScrollPane3);

        RegisteredPeersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Sharing Files..."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane8.setViewportView(RegisteredPeersTable);

        jSplitPane1.setRightComponent(jScrollPane8);

        RegisteredTorrentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Torrent File", "File Size", "Number of Chunks"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane9.setViewportView(RegisteredTorrentsTable);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Registered Peers:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Registered Torrent Files:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(39, 39, 39)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 486, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Tracker                        ", jPanel1);

        jMenu1.setText("File");

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        jMenu1.add(Exit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Config");

        Config.setText("Open Configuration Dialog");
        Config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConfigActionPerformed(evt);
            }
        });
        jMenu2.add(Config);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(SearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(SearchBox, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 913, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchButton)
                    .addComponent(SearchBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_ExitActionPerformed

    private void ConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConfigActionPerformed
        config.setLocationRelativeTo(null);
        config.setVisible(true);
    }//GEN-LAST:event_ConfigActionPerformed

    private void SearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchButtonActionPerformed

        searchFileName = SearchBox.getText();
        
        try {
            hostingPeers = host.Search(searchFileName);
        } catch (InterruptedException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (hostingPeers != null) {
            QueryResultsHeader.setText("Query results for " + searchFileName);
            DefaultTableModel model = (DefaultTableModel) DownloadChunks.getModel();
            model.setRowCount(0);
            for (int i = 0; i < hostingPeers.length; i++) {
                model.addRow(new Object[]{i, hostingPeers[i].clientIp, hostingPeers[i].creditForThem});
            }
            SearchResults.setModel(model);
        } else {
            QueryResultsHeader.setText("No results for " + searchFileName);
        }
    }//GEN-LAST:event_SearchButtonActionPerformed

    private void DownloadFromPeerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadFromPeerActionPerformed

        if ( hostingPeers == null ) {
            JOptionPane.showMessageDialog(gui,
                "There are no peers hosting this file!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rowIndex = SearchResults.getSelectedRow();
        if ( rowIndex < 0 ) {
            host.StartDownload(searchFileName);
        } else {
            host.StartDownload(searchFileName, hostingPeers[rowIndex]);
        }
    }//GEN-LAST:event_DownloadFromPeerActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                gui = GUI.getInstance();
                gui.setLocationRelativeTo(null);
                gui.setVisible(true);
                config.setLocationRelativeTo(null);
                config.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Config;
    private javax.swing.JTable DownloadChunks;
    private javax.swing.JList DownloadFileList;
    private javax.swing.JButton DownloadFromPeer;
    private javax.swing.JPanel Downloads;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JProgressBar FileDownloadProgress;
    private javax.swing.JProgressBar FileUploadProgress;
    private javax.swing.JPanel HostedFiles;
    private javax.swing.JTextArea HostedFilesList;
    private javax.swing.JPanel QueryResults;
    private javax.swing.JTextField QueryResultsHeader;
    private javax.swing.JList RegisteredPeersList;
    private javax.swing.JTable RegisteredPeersTable;
    private javax.swing.JTable RegisteredTorrentsTable;
    private javax.swing.JTextField SearchBox;
    private javax.swing.JButton SearchButton;
    private javax.swing.JTable SearchResults;
    private javax.swing.JTable UploadChunks;
    private javax.swing.JList UploadFileList;
    private javax.swing.JPanel Uploads;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
