/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.edge;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ResultContainer {
    public static final int STATUS_SUCCESS = 10;
    public static final int STATUS_FAIL    = 11;
    public static final int STATUS_TIMEOUT  = 12;
    public static final int STATUS_UNAUTHORIZED  = 13;

    private final int messageID;
    private Object result = null;
    private int status = STATUS_FAIL;
    private int opCode = -1;
    private final boolean synchronize;
    private EdgeFrame frame = null;
    private int retry = 0;
    private long timestamp = System.currentTimeMillis();

    public ResultContainer(EdgeNode eNode, boolean synchronize){
        this.messageID = eNode.getNextMessageID();
        this.synchronize = synchronize;
    }

    public boolean isSynchronize(){
        return this.synchronize;
    }

    public Object getResult() {
        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setFrame(EdgeFrame frame){
        this.frame = frame;
        this.opCode = this.frame.getApiOperatrion();
    }

    public EdgeFrame getFrame(){
        return this.frame;
    }

    public boolean isSuccess(){
        return this.status==STATUS_SUCCESS;
    }

    public boolean isFail(){
        return this.status==STATUS_FAIL;
    }

    public boolean isTimeout(){
        return this.status==STATUS_TIMEOUT;
    }

    public boolean isUnauthorized(){
        return this.status==STATUS_UNAUTHORIZED;
    }

    public void setSuccess(){
        this.status = STATUS_SUCCESS;
    }

    public void setFail(){
        this.status = STATUS_FAIL;
    }

    public void setTimeout(){
        this.status = STATUS_TIMEOUT;
    }

    public void setUnAuthorized(){
        this.status = STATUS_UNAUTHORIZED;
    }

    public boolean shouldRetry(){
        retry++;
        if(retry<3){
            return true;
        }
        return false;
    }

    public boolean isDone(){
        return retry>=3;
    }

    public int getRetryCount(){
        return this.retry;
    }
}
