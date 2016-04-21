/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import org.datasand.disk.tasks.JobStatusEnum;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class Job implements Runnable{
    protected final JobsTableModel model;
    private JobStatusEnum status = JobStatusEnum.JOB_STATUS_NOT_STARTER;
    private static int nextJobID = 100;
    private final int jobId;

    public Job(JobsTableModel model){
        this.model=model;
        synchronized(Job.class){
            nextJobID++;
            this.jobId = nextJobID;
        }
    }

    public abstract String getJobName();

    public void setStatus(JobStatusEnum status){
        this.status = status;
    }

    public JobStatusEnum getJobStatus(){
        return this.status;
    }

    public int getJobId(){
        return this.jobId;
    }
}
