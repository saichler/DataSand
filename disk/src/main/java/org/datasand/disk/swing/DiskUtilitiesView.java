/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.datasand.disk.DiskUtilitiesController;
import org.datasand.disk.model.DirectoryNode;
import org.datasand.disk.model.DirectoryObserver;
import org.datasand.disk.model.FileTableModel;
import org.datasand.disk.tasks.SumFilesInDirectoryTask;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DiskUtilitiesView extends JFrame implements DirectoryObserver,KeyListener,ActionListener,TreeSelectionListener{
    private JTree tree = new JTree();
    private JTable table = new JTable();
    private JTextField status = new JTextField();
    private JTextField seekPath = new JTextField("./");
    private JButton btnDeleteTargetDir = new JButton("Delete target dirs");

    private long lastUpdate = System.currentTimeMillis();
    private boolean updating = false;

    private DirectoryNode root = null;

    public DiskUtilitiesView(){
        this.setTitle("Disk Utils - For the Maven And Karaf developer");
        this.getContentPane().setLayout(new BorderLayout());
        this.tree.setShowsRootHandles(true);
        this.setSize(1024,768);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(new JScrollPane(tree));
        split.setRightComponent(new JScrollPane(table));
        this.getContentPane().add(split,BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(this.status,BorderLayout.SOUTH);
        bottomPanel.add(this.btnDeleteTargetDir,BorderLayout.EAST);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        this.getContentPane().add(this.seekPath,BorderLayout.NORTH);
        this.seekPath.addKeyListener(this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.root = new DirectoryNode(null,new File("./"),DiskUtilitiesView.this);
        MyTreeModel model = new MyTreeModel();
        tree.setModel(model);
        this.btnDeleteTargetDir.addActionListener(this);
        GUISettings.center(this);
        split.setDividerLocation(300);
        tree.addTreeSelectionListener(this);
        this.setVisible(true);
    }

    public void go(String path){
        this.lastUpdate = System.currentTimeMillis();
        status.setText("Working on:\""+path+"\"...");
        long start = System.currentTimeMillis();
        this.root = new DirectoryNode(null,new File(path),DiskUtilitiesView.this);
        DiskUtilitiesController.collect(root);
        DiskUtilitiesController.compute(root,true);
        MyTreeModel model = new MyTreeModel();
        long end = System.currentTimeMillis();
        System.gc();
        long memory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/ DiskUtilitiesController.MEG;
        status.setText("Done! Took "+(end-start) + " Memory="+memory+"m");
        tree.setModel(model);
    }

    private class MyTreeModel implements TreeModel {

        private MyTreeModel(){
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((DirectoryNode)parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((DirectoryNode)parent).getChildCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((DirectoryNode)node).isLeaf();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((DirectoryNode)parent).getIndex((DirectoryNode)child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {

        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {

        }
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
                                while (size < DiskUtilitiesController.threadPool.getNumberOfThreads()) {
                                    synchronized (SumFilesInDirectoryTask.pauseSync) {
                                        size = SumFilesInDirectoryTask.pauseCount;
                                    }
                                    try {
                                        Thread.sleep(50);
                                    } catch (Exception err) {
                                    }
                                }
                                DiskUtilitiesController.compute(root, true);
                                tree.setModel(new MyTreeModel());
                                synchronized (SumFilesInDirectoryTask.pauseSync) {
                                    SumFilesInDirectoryTask.pause = false;
                                    lastUpdate = System.currentTimeMillis();
                                    updating = false;
                                    SumFilesInDirectoryTask.pauseSync.notifyAll();
                                }
                            }
                        };
                        new Thread(runthis).start();
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
            Runnable runthis = new Runnable() {
                @Override
                public void run() {
                    go(seekPath.getText());
                }
            };
            new Thread(runthis).start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource()==btnDeleteTargetDir){
            btnDeleteTargetDir.setEnabled(false);
            status.setText("Seeking target directories...");
            Runnable runthis = new Runnable() {
                @Override
                public void run() {
                    DiskUtilitiesController.scanAndDeleteTargetDirectory(root.getDirectoryFile(),DiskUtilitiesView.this);
                    go(seekPath.getText());
                    btnDeleteTargetDir.setEnabled(true);
                }
            };
            new Thread(runthis).start();
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DirectoryNode dirNode = (DirectoryNode) e.getPath().getLastPathComponent();
        if(dirNode!=null){
            FileTableModel m = new FileTableModel(dirNode);
            this.table.setModel(m);
        }
    }

    public static void main(String args[]){
        GUISettings.setUI();
        new DiskUtilitiesView();
    }
}
