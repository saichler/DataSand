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
import org.datasand.disk.DiskUtilitiesController.FileComparatorSize;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class FileTableModel implements TableModel{


    private final List<File> files = new ArrayList<>();

    public FileTableModel(DirectoryNode dn){
        File[] temp = dn.getDirectoryFile().listFiles();
        for(File f:temp){
            if(!f.isDirectory()){
                this.files.add(f);
            }
        }
        Collections.sort(files,new FileComparatorSize());
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
                return "Size";
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
}
