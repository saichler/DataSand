/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cnode;

import org.datasand.agents.Message;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ICNodeCommandHandler<DataType,DataTypeElement> {
    public void handleMessage(Message cNodeCommand,NetworkID source,NetworkID destination,CPeerEntry<DataType> peerEntry,CNode<DataType,DataTypeElement> node);
    public void handleUnreachableMessage(Message cNodeCommand,NetworkID unreachableSource,CPeerEntry<DataType> peerEntry,CNode<DataType,DataTypeElement> node);
}
