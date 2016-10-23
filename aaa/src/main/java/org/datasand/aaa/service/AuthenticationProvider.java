package org.datasand.aaa.service;

import org.datasand.codec.util.ThreadNode;
import org.datasand.network.nnode.ConnectProtocol;
import org.datasand.network.nnode.Node;
import org.datasand.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by saichler on 10/23/16.
 */
public class AuthenticationProvider extends ThreadNode{

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationProvider.class);
    private static final int MAX_RSA_KEY_SIZE = 1024;
    private ServerSocket authSocket;
    private final Node node;
    private final byte[] rsaPubKeyBytes;

    public AuthenticationProvider(ThreadNode parent,Node node) {
        super(parent,"Authentication Provider");
        this.node = node;
        this.rsaPubKeyBytes = SecurityUtils.getRSAPublicKeyBytes(this.node.getRSAKeys().getPublic());
    }

    @Override
    public void initialize() {
        openServerSocket(null);
    }

    @Override
    public void execute() throws Exception {
        Socket s = authSocket.accept();
        DataInputStream in = new DataInputStream(s.getInputStream());
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        out.writeInt(rsaPubKeyBytes.length);
        out.write(rsaPubKeyBytes);
        LOG.info("RSA Size="+rsaPubKeyBytes.length);
        out.flush();
        ConnectProtocol.waitForInput(in);
        int size = in.readInt();
        if(size>MAX_RSA_KEY_SIZE){
            LOG.error("Someone tryied to pass a large size for RSA Key!!!");
            openServerSocket(s);
        }else {
            byte encAuthString[] = new byte[size];
            in.read(encAuthString);
            AuthString auth = AuthString.decrypt(encAuthString,this.node.getRSAKeys().getPrivate());
            if(auth==null){
                LOG.error("Failed to decrypt authentication string!!!");
                openServerSocket(s);
            }else if(doAuthenticate(auth)){
                size = in.readInt();
                if(size>MAX_RSA_KEY_SIZE) {
                    LOG.error("Someone tryied to pass a large size for RSA Key!!!");
                    openServerSocket(s);
                }else{
                    byte[] clientPubKeyBytes = new byte[size];
                    in.read(clientPubKeyBytes);
                    PublicKey clientPublicKey = SecurityUtils.getRSAPublicKeyFromBytes(clientPubKeyBytes);
                    SecretKey tempKey = SecurityUtils.generateSecretKey();
                    this.node.addTempKey(auth.getUsername(),tempKey);
                    byte data[] = SecurityUtils.encryptRSA(SecurityUtils.getSecretKeyBytes(tempKey),clientPublicKey);
                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();
                    s.close();
                }
            }
        }
    }

    public boolean doAuthenticate(AuthString as){
        if(as.getUsername().equals("admin") && as.getUsername().equals("admin")){
            return true;
        }
        return false;
    }

    private final void openServerSocket(Socket s){
        try{
            if(this.authSocket!=null) {
                authSocket.close();
            }
            if(s!=null) {
                s.close();
            }
        }catch(Exception e){
            LOG.debug("some error in closing the authentication socket",e);
        } finally {
            try {
                authSocket = new ServerSocket(60000);
            }catch(IOException e){
                LOG.error("Failed to initialize authentication provider, auth will not be available.",e);
            }
        }
    }

    public static SecretKey authenticate(String user, String pass, String host, KeyPair rsaKeys) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException, InterruptedException {
        Socket s = null;
        try {
            s = new Socket(host, 60000);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ConnectProtocol.waitForInput(in);
            int size = in.readInt();
            byte[] pubKeyData = new byte[size];
            in.read(pubKeyData);
            PublicKey pubKey = SecurityUtils.getRSAPublicKeyFromBytes(pubKeyData);
            AuthString auth = new AuthString(user, pass);
            byte[] encAuthString = auth.encrypt(pubKey);
            out.writeInt(encAuthString.length);
            out.write(encAuthString);
            out.flush();
            byte[] myPubKeyData = SecurityUtils.getRSAPublicKeyBytes(rsaKeys.getPublic());
            out.writeInt(myPubKeyData.length);
            out.write(myPubKeyData);
            out.flush();
            size = in.readInt();
            byte[] myKeyEnc = new byte[size];
            in.read(myKeyEnc);
            byte myKey[] = SecurityUtils.decryptRSA(myKeyEnc,rsaKeys.getPrivate());
            SecretKey key = SecurityUtils.getSecretKeyFromBytes(myKey);
            return key;
        }finally {
            if(s!=null){
                s.close();
            }
        }
    }

    @Override
    public void distruct() {

    }
}
