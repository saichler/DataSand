/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 *
 * Message is the vessel which is used to transfer data from one node to another.
 */
public class Message implements ISerializer{

    private long messageID = -1;
    private int messageType = -1;
    private Object messageData = null;
    private static long nextMessageID = 1000;
    public static final Map<Integer,Integer> passThroughIncomingMessage = new ConcurrentHashMap<Integer, Integer>();
    public static final Map<Integer,Integer> passThroughOutgoingMessage = new ConcurrentHashMap<Integer, Integer>();

    public Message(){
    }

    public Message(long _messageID,int _messageType,Object _messageData){
        this.messageID = _messageID;
        this.messageType = _messageType;
        this.messageData = _messageData;
    }

    public Message(int _messageType,Object _messageData){
        synchronized(Message.class){
            this.messageID = nextMessageID;
            nextMessageID++;
        }
        this.messageType = _messageType;
        this.messageData = _messageData;
    }

    public long getMessageID() {
        return messageID;
    }

    public int getMessageType() {
        return messageType;
    }

    public Object getMessageData() {
        return messageData;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        Message m = (Message)value;
        Encoder.encodeInt32(m.getMessageType(), ba);
        Encoder.encodeInt64(m.messageID, ba);
        if(passThroughOutgoingMessage.containsKey(m.getMessageType()) && m.getMessageData() instanceof BytesArray){
        	//this is a passthrough message
        	//copy the data bytes from the other container to this message
        	//2 - for the message class type
        	//4 - for the message type
        	//8 - for the message id
        	// == 14
        	ba.insert((BytesArray)m.getMessageData(),14);
        }else
        	Encoder.encodeObject(m.messageData, ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        Message m = new Message();
        m.messageType = Encoder.decodeInt32(ba);
        m.messageID = Encoder.decodeInt64(ba);
        if(passThroughIncomingMessage.containsKey(m.messageType)){
        	m.messageData = ba;
        }else{
        	m.messageData = Encoder.decodeObject(ba);
        }
        return m;
    }
}
