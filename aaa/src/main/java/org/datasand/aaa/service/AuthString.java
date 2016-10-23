package org.datasand.aaa.service;

import org.datasand.security.SecurityUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by saichler on 10/23/16.
 */
public class AuthString {
    private final String username;
    private final String password;

    public AuthString(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public final byte[] encrypt(PublicKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        String authString = username+":::"+password;
        return SecurityUtils.encryptRSA(authString.getBytes(),key);
    }

    public static final AuthString decrypt(byte [] encData, PrivateKey key) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte decData[] = SecurityUtils.decryptRSA(encData,key);
        String authString = new String(decData);
        int index = authString.indexOf(":::");
        if(index==-1){
            return null;
        }
        String username = authString.substring(0,index).trim();
        String password = authString.substring(index+3).trim();
        return new AuthString(username,password);
    }
}
