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

    private final SecretKey key;
    private final Map<NID,SecretKey> clients;

    public ConnectProtocol(SecretKey key,Map<NID,SecretKey> clients){
        this.key = key;
        this.clients = clients;
    }

    private static final String CONNECT_MESSAGE = "Hello, This is the connect message.";

    public boolean connect(Socket socket){
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            byte[] encrypt = SecurityUtils.encrypt(CONNECT_MESSAGE.getBytes(), key);
            out.writeInt(encrypt.length);
            out.write(encrypt);
            out.flush();
            int count = 0;
            while(in.available()==0){
                Thread.sleep(500);
                count++;
                if(count==20){
                    throw new IllegalStateException("Timeout while waiting for connet response");
                }
            }
            int size = in.readInt();
            byte data[] = new byte[size];
            in.read(data);
            byte[] unencrypted = SecurityUtils.decrypt(data,key);
            String connStr = new String(unencrypted);
            if(connStr.equals(CONNECT_MESSAGE)){
                return true;
            }else {
                if(clients!=null) {
                    for (SecretKey csk : clients.values()) {

                    }
                }
                socket.close();
                return false;
            }
        }catch(Exception e){
            LOG.error("Failed to connect due to:",e);
        }
        return false;
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
