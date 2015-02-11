package org.datasand.agents.cnode.handlers;

import org.datasand.agents.Message;
import org.datasand.agents.MessageEntry;
import org.datasand.agents.cnode.CNode;
import org.datasand.agents.cnode.CPeerEntry;
import org.datasand.agents.cnode.ICNodeCommandHandler;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AcknowledgeHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{
    @Override
    public void handleMessage(Message cNodeCommand, NetworkID source,NetworkID destination, CPeerEntry<DataType> peerEntry,CNode<DataType, DataTypeElement> node) {
        MessageEntry entry = node.getJournalEntry(cNodeCommand);
        if(entry!=null){
            entry.removePeer(source);
            if(entry.isFinished()){
                node.removeJournalEntry(cNodeCommand);
            }
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand,NetworkID unreachableSource, CPeerEntry<DataType> peerEntry,CNode<DataType, DataTypeElement> node) {
    }
}
