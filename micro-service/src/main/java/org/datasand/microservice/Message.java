/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 *         <p>
 *         Message is the vessel which is used to transfer data from one node to another.
 */
public class Message implements ISerializer {

    public static final Map<Integer, Integer> passThroughIncomingMessage = new ConcurrentHashMap<Integer, Integer>();
    public static final Map<Integer, Integer> passThroughOutgoingMessage = new ConcurrentHashMap<Integer, Integer>();

    static {
        Encoder.registerSerializer(Message.class, new Message(-1, -1, null));
    }

    private static long nextMessageID = 1000;

    private final long messageID;
    private final int messageType;
    private Object messageData = null;

    public Message(long messageID, int messageType, Object messageData) {
        this.messageID = messageID;
        this.messageType = messageType;
        this.messageData = messageData;
    }

    public Message(int messageType, Object messageData) {
        synchronized (Message.class) {
            this.messageID = nextMessageID;
            nextMessageID++;
        }
        this.messageType = messageType;
        this.messageData = messageData;
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
        Message m = (Message) value;
        Encoder.encodeInt16(m.getMessageType(), ba);
        Encoder.encodeInt64(m.messageID, ba);
        if (passThroughOutgoingMessage.containsKey(m.getMessageType()) && m.getMessageData() instanceof BytesArray) {
            //this is a passthrough message
            //copy the data bytes from the other container to this message
            //2 - for the message class type
            //4 - for the message type
            //8 - for the message id
            // == 14
            ba.insert((BytesArray) m.getMessageData(), 14);
        } else
            Encoder.encodeObject(m.messageData, ba);
    }

    @Override
    public Object decode(BytesArray ba) {

        int type = Encoder.decodeInt16(ba);
        long id = Encoder.decodeInt64(ba);
        Object data = null;
        if (passThroughIncomingMessage.containsKey(type)) {
            data = ba;
        } else {
            data = Encoder.decodeObject(ba);
        }

        Message m = new Message(id, type, data);
        return m;
    }
}
