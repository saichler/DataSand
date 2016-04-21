/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.tasks;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public enum JobStatusEnum {
    JOB_STATUS_NOT_STARTER,
    JOB_STATUS_RUNNING,
    JOB_STATUS_FINISHED_SUCCESSFULY,
    JOB_STATUS_FINISHED_ERROR,
    JOB_STATUS_PAUSED;

    public String toString(){
        switch (this){
            case JOB_STATUS_FINISHED_ERROR:
                return "Finished With an Error.";
            case JOB_STATUS_FINISHED_SUCCESSFULY:
                return  "Finished Successfuly";
            case JOB_STATUS_NOT_STARTER:
                return "Not Started";
            case JOB_STATUS_PAUSED:
                return "Job Paused";
            case JOB_STATUS_RUNNING:
                return "Running";
        }
        return "Error";
    }
}
