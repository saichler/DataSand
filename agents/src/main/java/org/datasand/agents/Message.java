package org.datasand.agents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.datasand.codec.EncodeDataContainer;
import org.datasand.codec.ISerializer;
import org.datasand.codec.bytearray.ByteArrayEncodeDataContainer;
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
    public void encode(Object value, byte[] byteArray, int location) {

    }

    @Override
    public void encode(Object value, EncodeDataContainer edc) {
        Message m = (Message)value;
        edc.getEncoder().encodeInt32(m.getMessageType(), edc);        
        edc.getEncoder().encodeInt64(m.messageID, edc);
        if(passThroughOutgoingMessage.containsKey(m.getMessageType()) && m.getMessageData() instanceof ByteArrayEncodeDataContainer){
        	//this is a passthrough message
        	//copy the data bytes from the other container to this message
        	//2 - for the message class type
        	//4 - for the message type
        	//8 - for the message id
        	// == 14
        	((ByteArrayEncodeDataContainer)edc).insert((ByteArrayEncodeDataContainer)m.getMessageData(),14);
        }else
        	edc.getEncoder().encodeObject(m.messageData, edc);
    }

    @Override
    public Object decode(byte[] byteArray, int location, int length) {
        return null;
    }

    @Override
    public Object decode(EncodeDataContainer edc, int length) {
        Message m = new Message();
        m.messageType = edc.getEncoder().decodeInt32(edc);        
        m.messageID = edc.getEncoder().decodeInt64(edc);
        if(passThroughIncomingMessage.containsKey(m.messageType)){
        	m.messageData = edc;
        }else{
        	m.messageData = edc.getEncoder().decodeObject(edc);
        }
        return m;
    }

    @Override
    public String getShardName(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getRecordKey(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }
}
