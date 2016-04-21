/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.tasks;

import org.datasand.disk.DiskUtilitiesController;
import org.datasand.disk.model.DirectoryNode;
import org.datasand.disk.model.DirectoryScanListener;
import org.datasand.disk.model.Job;
import org.datasand.disk.model.JobsTableModel;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DeleteTargetDirectoryJob extends Job{
    private final DirectoryNode directoryNode;
    private final DirectoryScanListener listener;
    public DeleteTargetDirectoryJob(JobsTableModel model, DirectoryNode directoryNode, DirectoryScanListener listener){
        super(model);
        this.directoryNode = directoryNode;
        this.listener = listener;
        model.addJob(this);
    }

    public void run(){
        setStatus(JobStatusEnum.JOB_STATUS_RUNNING);
        scanAndDeleteTargetDirectory(directoryNode,listener);
        setStatus(JobStatusEnum.JOB_STATUS_FINISHED_SUCCESSFULY);
    }

    public static void scanAndDeleteTargetDirectory(DirectoryNode directoryNode, DirectoryScanListener observer){
        if(directoryNode.getDirectoryFile().getName().equals("target")){
            observer.observe(directoryNode.getDirectoryFile(),2);
            DiskUtilitiesController.deleteDirectory(directoryNode.getDirectoryFile());
        } else {
            for(DirectoryNode dir:directoryNode.getChildren()){
                scanAndDeleteTargetDirectory(dir,observer);
            }
        }
    }

    @Override
    public String getJobName() {
        return "\"target\" directories delete at "+directoryNode.getDirectoryFile().getName();
    }
}
