/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode;

import org.datasand.microservice.Message;
import org.datasand.network.NetUUID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ICNodeCommandHandler<DataType,DataTypeElement> {
    public void handleMessage(Message cNodeCommand, MicroServiceNetUUID source, MicroServiceNetUUID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType,DataTypeElement> node);
    public void handleUnreachableMessage(Message cNodeCommand, NetUUID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType,DataTypeElement> node);
}
