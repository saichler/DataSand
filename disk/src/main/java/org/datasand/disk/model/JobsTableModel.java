/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JobsTableModel implements TableModel{

    private final List<Job> rows = new ArrayList<>();
    private final List<TableModelListener> listeners = new ArrayList<>();
    private final Map<Integer,Integer> jobId2row = new HashMap<>();
    private long lastEventFire = System.currentTimeMillis();

    public void clear(){
        rows.clear();
        jobId2row.clear();
        TableModelEvent e = new TableModelEvent(this);
        for(TableModelListener l:this.listeners){
            l.tableChanged(e);
        }
    }

    public void addJob(Job job){
        this.jobId2row.put(job.getJobId(),rows.size());
        this.rows.add(job);
        notifyRowAdded(rows.size()-1);
    }

    public void notifyRowAdded(int row){
        if(System.currentTimeMillis()-lastEventFire>2000) {
            synchronized(this) {
                lastEventFire = System.currentTimeMillis();
                TableModelEvent e = new TableModelEvent(this, row, row, 0, TableModelEvent.INSERT);
                for (TableModelListener l : this.listeners) {
                    l.tableChanged(e);
                }
            }
        }
    }

    public void notifyStatusChanged(Job job){
        if(System.currentTimeMillis()-lastEventFire>2000) {
            synchronized(this) {
                lastEventFire = System.currentTimeMillis();
                int row = this.jobId2row.get(job.getJobId());
                TableModelEvent e = new TableModelEvent(this, row, row, 1, TableModelEvent.UPDATE);
                for (TableModelListener l : this.listeners) {
                    l.tableChanged(e);
                }
            }
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex){
            case 0:
                return "Job Name";
            case 1:
                return "Status";
            case 2:
                return "Row Index";
        }
        return "error";
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
        Job job = rows.get(rowIndex);
        switch(columnIndex){
            case 0:
                return job.getJobName();
            case 1:
                return job.getJobStatus().toString();
            case 2:
                return ""+rowIndex;
        }
        return "Error";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        this.listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.listeners.remove(l);
    }
}
