package org.datasand.backup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by root on 2/15/16.
 */
public class BackupGUI extends JFrame implements Backup.TaskListener, ActionListener{

    private final JTextField fromPath = new JTextField("/Backups");
    private final JTextField toPath = new JTextField("/media/root/HD-GDU3/Backups");
    private final JTextField status = new JTextField("");
    private final JButton btnGo = new JButton("GO");
    private File current = null;

    public BackupGUI(){
        this.setTitle("Backup Files");
        this.setSize(800,600);
        this.setVisible(true);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(status,BorderLayout.SOUTH);
        this.getContentPane().add(btnGo,BorderLayout.NORTH);
        btnGo.addActionListener(this);
    }

    public static void main(String args[]){
        BackupGUI gui = new BackupGUI();
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    @Override
    public void currentFile(File f) {
        this.current = f;
    }

    @Override
    public void status(String str) {
        this.status.setText(str);
    }

    @Override
    public void setCopySize(long size) {
        this.status.setText("File:"+current.getPath()+" copied "+(size*1024*1024)+" out of "+this.current.length());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Backup b = new Backup(this.fromPath.getText(),this.toPath.getText(),this);
    }
}
