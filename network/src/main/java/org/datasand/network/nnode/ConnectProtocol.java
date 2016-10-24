package org.datasand.network.nnode;

import org.datasand.network.NID;
import org.datasand.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by saichler on 10/21/16.
 */
public class ConnectProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectProtocol.class);
    public static final int SWITCH_CONNECTION = 2;
    public static final int CLIENT_CONNECTION = 3;
    public static final int NO_CONNECTION = -2;

    private final SecretKey key;
    private final Map<String,SecretKey> clients;


    public ConnectProtocol(SecretKey key,Map<String,SecretKey> clients){
        this.key = key;
        this.clients = clients;
    }

    private static final String CONNECT_MESSAGE = "Hello, This is the connect message.";

    public int connect(Socket socket, boolean connectTo){
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            byte[] encrypt = SecurityUtils.encrypt(CONNECT_MESSAGE.getBytes(), key);
            out.writeInt(encrypt.length);
            out.write(encrypt);
            out.flush();
            ConnectProtocol.waitForInput(in);
            int size = in.readInt();
            byte data[] = new byte[size];
            in.read(data);
            byte[] unencrypted = SecurityUtils.decrypt(data,key);
            String connStr = new String(unencrypted);
            if(connStr.equals(CONNECT_MESSAGE)){
                return SWITCH_CONNECTION;
            }else {
                if(isClientKey(data)){
                    out.writeInt(CLIENT_CONNECTION);
                    out.flush();
                    return CLIENT_CONNECTION;
                } else if(connectTo){
                    out.writeInt(NO_CONNECTION);
                    out.flush();
                } else {
                    int stat = in.readInt();
                    if(stat==CLIENT_CONNECTION){
                        return CLIENT_CONNECTION;
                    }
                }
                socket.close();
                return NO_CONNECTION;
            }
        }catch(Exception e){
            LOG.error("Failed to connect due to:",e);
        }
        return NO_CONNECTION;
    }

    private final boolean isClientKey(byte data[]) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        if(this.clients!=null) {
            for(SecretKey clientKey:clients.values()) {
                byte[] unencrypted = SecurityUtils.decrypt(data, clientKey);
                String connStr = new String(unencrypted);
                if(connStr.equals(CONNECT_MESSAGE)){
                    return true;
                }
            }
        }
        return false;
    }

    public static final void waitForInput(DataInputStream in) throws InterruptedException, IOException {
        int count = 0;
        while(in.available()==0){
            Thread.sleep(500);
            count++;
            if(count==20){
                throw new IllegalStateException("Timeout while waiting for input stream");
            }
        }
    }

    private static final boolean ok(SecretKey key,byte[] data) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        byte[] unencrypted = SecurityUtils.decrypt(data,key);
        String connStr = new String(unencrypted);
        if(connStr.equals(CONNECT_MESSAGE)){
            return true;
        }
        return false;
    }
}
