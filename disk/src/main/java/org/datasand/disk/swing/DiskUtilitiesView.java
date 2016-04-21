/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.swing;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.datasand.disk.DiskUtilitiesController;
import org.datasand.disk.model.DirectoryNode;
import org.datasand.disk.model.DirectoryScanListener;
import org.datasand.disk.model.DirectoryTreeModel;
import org.datasand.disk.model.FileTableModel;
import org.datasand.disk.model.JobsTableModel;
import org.datasand.disk.model.SyncDataListener;
import org.datasand.disk.tasks.DeleteTargetDirectoryJob;
import org.datasand.disk.tasks.SumFilesInDirectoryTask;
import org.datasand.disk.tasks.SyncFilesTask;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DiskUtilitiesView extends JFrame implements DirectoryScanListener,KeyListener,ActionListener,TreeSelectionListener,MouseListener,SyncDataListener, TableModelListener{
    private JTree tree = new JTree();
    private JTable table = new JTable();
    private JTable jobs = new JTable();
    private JobsTableModel jobsTableModel = new JobsTableModel();
    private JTextField status = new JTextField();
    private JTextField txtPath = new JTextField("./");
    private JButton btnDeleteTargetDir = new JButton("Delete target dirs");
    private JButton btnGO = new JButton("GO!");

    private long lastUpdate = System.currentTimeMillis();
    private boolean updating = false;

    private DirectoryNode root = null;

    private JPopupMenu treePopupMenu = new JPopupMenu();
    private JPopupMenu tablePopupMenu = new JPopupMenu();

    private int currentTreeSort = 1;
    private int currentTableSort = 1;

    private DirectoryNode currentTableDirNode = null;

    public DiskUtilitiesView(){
        this.setTitle("Disk Utils - For the Maven And Karaf developer");
        this.getContentPane().setLayout(new BorderLayout());
        this.tree.setShowsRootHandles(true);
        this.setSize(1024,768);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(new JScrollPane(tree));
        split.setRightComponent(new JScrollPane(table));
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split2.setTopComponent(split);
        JPanel jobPanel = new JPanel(new BorderLayout());
        jobPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Jobs"));
        jobPanel.add(new JScrollPane(jobs),BorderLayout.CENTER);
        split2.setBottomComponent(jobPanel);
        split2.setDividerLocation(500);
        this.getContentPane().add(split2,BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(this.status,BorderLayout.SOUTH);
        bottomPanel.add(this.btnDeleteTargetDir,BorderLayout.EAST);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        JPanel north = new JPanel(new BorderLayout());
        north.add(txtPath,BorderLayout.CENTER);
        north.add(new JLabel("Directory Path:"),BorderLayout.WEST);
        north.add(btnGO,BorderLayout.EAST);
        this.getContentPane().add(north,BorderLayout.NORTH);
        this.btnGO.addActionListener(this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.root = new DirectoryNode(null,new File("./"),DiskUtilitiesView.this);
        DirectoryTreeModel model = new DirectoryTreeModel(root);
        tree.setModel(model);
        this.btnDeleteTargetDir.addActionListener(this);
        GUISettings.center(this);
        split.setDividerLocation(300);
        tree.addTreeSelectionListener(this);
        setupTreePopupMenu();
        setupTablePopupMenu();
        table.getTableHeader().addMouseListener(this);
        jobs.setModel(jobsTableModel);
        jobsTableModel.addTableModelListener(this);
        this.setVisible(true);
    }

    private void setupTreePopupMenu(){
        JMenu sort = new JMenu("Sort");
        JMenuItem bySize = new JMenuItem("By Size");
        JMenuItem byName = new JMenuItem("By Name");
        sort.add(bySize);
        sort.add(byName);
        bySize.addActionListener(this);
        byName.addActionListener(this);
        treePopupMenu.add(sort);
        treePopupMenu.addSeparator();
        JMenuItem sync = new JMenuItem("Sync With...");
        treePopupMenu.add(sync);
        sync.addActionListener(this);
        treePopupMenu.addSeparator();
        JMenuItem delete = new JMenuItem("Delete Directory");
        delete.addActionListener(this);
        treePopupMenu.add(delete);
        tree.setComponentPopupMenu(treePopupMenu);
    }

    private void setupTablePopupMenu(){
        JMenuItem delete = new JMenuItem("Delete File");
        delete.addActionListener(this);
        tablePopupMenu.add(delete);
        table.setComponentPopupMenu(tablePopupMenu);
    }

    public void go(final String path){
        jobsTableModel.clear();
        Runnable runthis = new Runnable() {
            @Override
            public void run() {
                //DiskUtilitiesView.this.setEnabled(false);
                lastUpdate = System.currentTimeMillis();
                status.setText("Working on:\""+path+"\"...");
                long start = System.currentTimeMillis();
                root = new DirectoryNode(null,new File(path),DiskUtilitiesView.this);
                SumFilesInDirectoryTask.start(root,jobsTableModel);
                DiskUtilitiesController.compute(root,currentTreeSort);
                DirectoryTreeModel model = new DirectoryTreeModel(root);
                long end = System.currentTimeMillis();
                System.gc();
                long memory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/ DiskUtilitiesController.MEG;
                status.setText("Done! Took "+(end-start) + " Memory="+memory+"m");
                tree.setModel(model);
                tree.setSelectionPath(new TreePath(root));
                //DiskUtilitiesView.this.setEnabled(true);
            }
        };
        DiskUtilitiesController.addGUITask(runthis);
    }

    @Override
    public void observe(File file,int taskID) {
        if(taskID==2){
            status.setText("Deleting :"+file.getAbsolutePath());
        } else if (taskID==1) {
            if (!updating && System.currentTimeMillis() - lastUpdate > 5000) {
                synchronized (SumFilesInDirectoryTask.pauseSync) {
                    if (!updating && System.currentTimeMillis() - lastUpdate > 5000) {
                        SumFilesInDirectoryTask.pause = true;
                        this.updating = true;
                        Runnable runthis = new Runnable() {
                            @Override
                            public void run() {
                                int size = 0;
                                while (size < SumFilesInDirectoryTask.getThreadCount()) {
                                    synchronized (SumFilesInDirectoryTask.pauseSync) {
                                        size = SumFilesInDirectoryTask.pauseCount;
                                    }
                                    try {
                                        Thread.sleep(50);
                                    } catch (Exception err) {
                                    }
                                }
                                DiskUtilitiesController.compute(root, currentTreeSort);
                                tree.setModel(new DirectoryTreeModel(root));
                                synchronized (SumFilesInDirectoryTask.pauseSync) {
                                    SumFilesInDirectoryTask.pause = false;
                                    lastUpdate = System.currentTimeMillis();
                                    updating = false;
                                    SumFilesInDirectoryTask.pauseSync.notifyAll();
                                }
                            }
                        };
                        DiskUtilitiesController.addGUITask(runthis);
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER){
            go(txtPath.getText());
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() instanceof JMenuItem){
            String action = ((JMenuItem)actionEvent.getSource()).getText();
            if(action.equals("By Size")){
                this.currentTreeSort = 1;
                go(txtPath.getText());
            }else if (action.equals("By Name")){
                this.currentTreeSort = 2;
                go(txtPath.getText());
            }else if(action.equals("Delete File")){
                int tableRow = table.getSelectedRow();
                if(tableRow>=0){
                    File file = ((FileTableModel)table.getModel()).getFileAt(tableRow);
                    int yesno = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete file:\n"+file.getName(),"Delete File",JOptionPane.YES_NO_OPTION);
                    if(yesno==JOptionPane.YES_OPTION){
                        file.delete();
                        table.setModel(new FileTableModel(this.currentTableDirNode,this.currentTableSort));
                    }
                }
            }else if(action.equals("Delete Directory")){
                DirectoryNode node = (DirectoryNode)tree.getSelectionPath().getLastPathComponent();
                if(node!=null){
                    int yesno = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete directory:\n"+node.getDirectoryFile(),"Delete Directory",JOptionPane.YES_NO_OPTION);
                    if(yesno==JOptionPane.YES_OPTION){
                        DiskUtilitiesController.deleteDirectory(node.getDirectoryFile());
                        go(txtPath.getText());
                    }
                }
            }else if(action.equals("Sync With...")){
                DirectoryNode node = (DirectoryNode)tree.getSelectionPath().getLastPathComponent();
                if(node!=null) {
                    String dest = JOptionPane.showInputDialog(this,"Synchronize "+node.getDirectoryFile().getName()+" to destination:","Synchronize Directory");
                    if(dest!=null){
                        File destFile = new File(dest);
                        SyncFilesTask task = new SyncFilesTask(this,node.getDirectoryFile(),destFile,jobsTableModel);
                        jobsTableModel.clear();
                        jobsTableModel.addJob(task);
                        DiskUtilitiesController.addGUITask(task);
                    }
                }
            }
        }else
        if(actionEvent.getSource()==btnDeleteTargetDir){
            status.setText("Seeking target directories...");
            DeleteTargetDirectoryJob job = new DeleteTargetDirectoryJob(jobsTableModel,root,this);
            DiskUtilitiesController.addGUITask(job);
            DiskUtilitiesController.waitForGUITask();
            go(txtPath.getText());
        }else if(actionEvent.getSource()==btnGO){
            go(txtPath.getText());
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DirectoryNode dirNode = (DirectoryNode) e.getPath().getLastPathComponent();
        if(dirNode!=null){
            this.currentTableDirNode = dirNode;
            FileTableModel m = new FileTableModel(dirNode,this.currentTableSort);
            this.table.setModel(m);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource()==this.table.getTableHeader() && e.getClickCount()==2){
            int col = table.columnAtPoint(e.getPoint());
            if(currentTableDirNode!=null){
                if(col==0 && this.currentTableSort!=2){
                    Runnable runthis = new Runnable() {
                        @Override
                        public void run() {
                            currentTableSort = 2;
                            table.setModel(new FileTableModel(currentTableDirNode,2));
                        }
                    };
                    SwingUtilities.invokeLater(runthis);
                }else
                if(col==1 && this.currentTableSort!=1) {
                    Runnable runthis = new Runnable() {
                        @Override
                        public void run() {
                            currentTableSort = 1;
                            table.setModel(new FileTableModel(currentTableDirNode,1));
                        }
                    };
                    SwingUtilities.invokeLater(runthis);
                }else
                if(col==2 && this.currentTableSort!=3) {
                    Runnable runthis = new Runnable() {
                        @Override
                        public void run() {
                            currentTableSort = 3;
                            table.setModel(new FileTableModel(currentTableDirNode,3));
                        }
                    };
                    SwingUtilities.invokeLater(runthis);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void notifyCurrentDirectory(File directory) {
        this.status.setText(directory.getAbsolutePath());
    }

    @Override
    public void notifyCurrentFile(File file) {
        this.status.setText(file.getAbsolutePath());
    }

    @Override
    public void notifyCurrentFileProgress(File file, int part,int outOf) {
        double percent = part;
        percent = percent / outOf;
        percent = percent * 100;
        this.status.setText(DiskUtilitiesController.kFormat.format(percent)+"% - "+file.getAbsolutePath());
    }

    @Override
    public void notifyDone(File source, File dest) {
        this.status.setText("Done synchronizing source "+source.getName()+" to "+dest.getName()+".");
    }

    public static void main(String args[]){
        GUISettings.setUI();
        new DiskUtilitiesView();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if(e.getType()==TableModelEvent.INSERT){
            int row = e.getFirstRow();
            JViewport vp = (JViewport)jobs.getParent();
            Rectangle r = jobs.getCellRect(row, 1, true);
            Point p = vp.getViewPosition();
            r.setLocation(r.x-p.x, r.y-p.y);
            try {
                jobs.scrollRectToVisible(r);
            }catch(Exception err){}
        }
    }
}
