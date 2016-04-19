/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.datasand.disk.DiskUtilitiesController;
import org.datasand.disk.DiskUtilitiesController.FileComparatorDate;
import org.datasand.disk.DiskUtilitiesController.FileComparatorName;
import org.datasand.disk.DiskUtilitiesController.FileComparatorSize;
import org.datasand.disk.swing.DiskUtilitiesView;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class FileTableModel implements TableModel{


    private final List<File> files = new ArrayList<>();
    private final DirectoryNode directoryNode;
    private int size=0;

    public FileTableModel(DirectoryNode dn,int sortType){
        this.directoryNode = dn;
        File[] temp = dn.getDirectoryFile().listFiles();
        if(temp==null) return;
        for(File f:temp){
            if(!f.isDirectory()){
                this.files.add(f);
                size+=f.length();
            }
        }
        if(sortType==1) {
            Collections.sort(files, new FileComparatorSize());
        }else if(sortType==2){
            Collections.sort(files, new FileComparatorName());
        }else if(sortType==3){
            Collections.sort(files, new FileComparatorDate());
        }
    }

    public File getFileAt(int i){
        return files.get(i);
    }

    @Override
    public int getRowCount() {
        return files.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex){
            case 0:
                return "File Name";
            case 1:
                return "Size ("+getSize()+")";
            case 2:
                return "Date";
        }
        return "Error";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        File f = files.get(rowIndex);
        switch (columnIndex){
            case 0:
                return f.getName();
            case 1:
                return DiskUtilitiesController.getFileSize(f);
            case 2:
                return new Date(f.lastModified());
        }
        return "error";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }

    private String getSize(){
        double s = this.size;
        double k = s/ DiskUtilitiesController.K;
        double m = s/ DiskUtilitiesController.MEG;
        double g = s/ DiskUtilitiesController.GIG;
        StringBuilder sb = new StringBuilder();
        if(g>1){
            sb.append(DiskUtilitiesController.kFormat.format(g)).append("g");
        }else
        if(m>1){
            sb.append(DiskUtilitiesController.kFormat.format(m)).append("m");
        } else {
            sb.append(DiskUtilitiesController.kFormat.format(k)).append("k");
        }
        return sb.toString();
    }

}
