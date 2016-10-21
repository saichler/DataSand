package org.datasand.network.nnode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import javax.crypto.SecretKey;
import org.datasand.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saichler on 10/21/16.
 */
public class ConnectProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectProtocol.class);

    private final SecretKey key;

    public ConnectProtocol(SecretKey key){
        this.key = key;
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
                socket.close();
                return false;
            }
        }catch(Exception e){
            LOG.error("Failed to connect due to:",e);
        }
        return false;
    }
}
