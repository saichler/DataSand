/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.datasand.disk.Sizer.Directory;
import org.datasand.disk.Sizer.SizerVisitor;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DiskUtilities extends JFrame implements SizerVisitor,KeyListener{
    private JTree tree = new JTree();
    private JTextField status = new JTextField();
    private JTextField seekPath = new JTextField("./");

    private long lastUpdate = System.currentTimeMillis();
    private boolean updating = false;

    private Directory root = null;

    public DiskUtilities(){
        this.setTitle("Disk Utils");
        this.getContentPane().setLayout(new BorderLayout());
        this.tree.setShowsRootHandles(true);
        this.setSize(800,600);
        this.getContentPane().add(new JScrollPane(tree),BorderLayout.CENTER);
        this.getContentPane().add(this.status, BorderLayout.SOUTH);
        this.getContentPane().add(this.seekPath,BorderLayout.NORTH);
        this.seekPath.addKeyListener(this);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.root = new Directory(null,new File("./"),DiskUtilities.this);
        MyTreeModel model = new MyTreeModel();
        tree.setModel(model);
    }

    public void go(String path){
        this.lastUpdate = System.currentTimeMillis();
        status.setText("Working on:\""+path+"\"...");
        long start = System.currentTimeMillis();
        this.root = new Directory(null,new File(path),DiskUtilities.this);
        Sizer.collect(root);
        Sizer.compute(root,true);
        MyTreeModel model = new MyTreeModel();
        long end = System.currentTimeMillis();
        System.gc();
        long memory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/Sizer.MEG;
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
            return ((Directory)parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((Directory)parent).getChildCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((Directory)node).isLeaf();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((Directory)parent).getIndex((Directory)child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {

        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {

        }
    }

    @Override
    public void currentFile(File file) {
        if(!updating && System.currentTimeMillis()-lastUpdate>5000){
            synchronized(Sizer.pauseSync) {
                if(!updating && System.currentTimeMillis()-lastUpdate>5000) {
                    Sizer.pause = true;
                    this.updating = true;
                    Runnable runthis = new Runnable() {
                        @Override
                        public void run() {
                            int size = 0;
                            while (size < Sizer.threadPool.getNumberOfThreads()) {
                                synchronized(Sizer.pauseSync) {
                                    size = Sizer.pauseCount;
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (Exception err) {
                                }
                            }
                            Sizer.compute(root,true);
                            tree.setModel(new MyTreeModel());
                            synchronized (Sizer.pauseSync) {
                                Sizer.pause = false;
                                lastUpdate = System.currentTimeMillis();
                                updating = false;
                                Sizer.pauseSync.notifyAll();
                            }
                        }
                    };
                    new Thread(runthis).start();
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

    public static void main(String args[]){
        new DiskUtilities();
    }
}
