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
