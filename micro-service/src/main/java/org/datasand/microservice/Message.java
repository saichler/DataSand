/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

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

    private int microServiceID = -1;
    private long messageID = -1;
    private int messageType = -1;
    private Object messageData = null;
    private static long nextMessageID = 1000;
    public static final Map<Integer,Integer> passThroughIncomingMessage = new ConcurrentHashMap<Integer, Integer>();
    public static final Map<Integer,Integer> passThroughOutgoingMessage = new ConcurrentHashMap<Integer, Integer>();

    public Message(){
    }

    public Message(int microServiceID, long messageID,int messageType,Object messageData){
        this.microServiceID = microServiceID;
        this.messageID = messageID;
        this.messageType = messageType;
        this.messageData = messageData;
    }

    public Message(int microServiceID,int messageType,Object messageData){
        synchronized(Message.class){
            this.messageID = nextMessageID;
            nextMessageID++;
        }
        this.microServiceID = microServiceID;
        this.messageType = messageType;
        this.messageData = messageData;
    }

    public int getMicroServiceID(){
        return this.microServiceID;
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
        Encoder.encodeInt32(m.getMicroServiceID(),ba);
        Encoder.encodeInt32(m.getMessageType(), ba);
        Encoder.encodeInt64(m.messageID, ba);
        if(passThroughOutgoingMessage.containsKey(m.getMessageType()) && m.getMessageData() instanceof BytesArray){
        	//this is a passthrough message
        	//copy the data bytes from the other container to this message
            //2 - for the micro service id
        	//2 - for the message class type
        	//4 - for the message type
        	//8 - for the message id
        	// == 16
        	ba.insert((BytesArray)m.getMessageData(),16);
        }else
        	Encoder.encodeObject(m.messageData, ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        Message m = new Message();
        m.microServiceID = Encoder.decodeInt32(ba);
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
