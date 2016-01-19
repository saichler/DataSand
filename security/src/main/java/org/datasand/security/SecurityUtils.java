/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.datasand.codec.VLogger;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SecurityUtils {
    private static byte[] iv = { 0, 4, 0, 0, 6, 81, 0, 8, 71, 86, 5, 0, 0, 43, 0, 1};
    protected static IvParameterSpec ivspec = new IvParameterSpec(iv);
    //Encrypt a byte array using the given key.
    public static byte[] encrypt(byte[] dataToEncode,SecretKey key){
        try {
            Cipher cr = Cipher.getInstance("AES/CFB8/NoPadding");
            cr.init(Cipher.ENCRYPT_MODE, key, ivspec);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            CipherOutputStream out = new CipherOutputStream(bout, cr);
            out.write(dataToEncode);
            out.close();
            return bout.toByteArray();
        } catch (Exception err) {
            VLogger.error("Failed to encrypt byte array", err);
            return null;
        }
    }

    //Decrypt a byte array using the given key
    public static byte[] decrypt(byte[] dataToDecrypt,SecretKey key){
        try {
            Cipher cr = Cipher.getInstance("AES/CFB8/NoPadding");
            cr.init(Cipher.DECRYPT_MODE, key, ivspec);
            ByteArrayInputStream bin = new ByteArrayInputStream(dataToDecrypt);
            CipherInputStream in = new CipherInputStream(bin, cr);
            byte data[] = new byte[dataToDecrypt.length];
            in.read(data);
            in.close();
            return data;
        } catch (Exception err) {
            VLogger.error("Failed To Decrypt byte array.", err);
            return null;
        }
    }

    public static final SecretKey generateSecretKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            VLogger.error("Failed to generate a secret key.",e);
            return null;
        }
    }

    public static final KeyPair generateRSAKeys(){
    	try{
    	    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    	    keyPairGenerator.initialize(2048);
    	    return keyPairGenerator.genKeyPair();
    	  }catch(Exception e){
            VLogger.error("Failed to generate a RSA key.",e);
            return null;
    	}
    }

    public static final byte[] encryptRSA(byte dataToEncrypt[], PublicKey publicKey){
        //if the data is larger than 117 bytes. we need to encrypt it in two blocks
        //we divide to 110 per block
        byte blockCount = 0;
        if(dataToEncrypt.length>110){
            blockCount = (byte)(dataToEncrypt.length/110+1);
        }
    	try{
    	    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    	    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    	    if(blockCount==0){
    	        byte encData[] = cipher.doFinal(dataToEncrypt);
    	        byte[] result = new byte[encData.length+1];
    	        result[0] = (byte)encData.length;
    	        System.arraycopy(encData, 0, result, 1, encData.length);
    	        return result;
    	    }else{
    	        List<byte[]> blocks = new LinkedList<>();
    	        for(int i=0;i<blockCount;i++){
    	            if(dataToEncrypt.length-i*110>=110){
    	                byte data[] = new byte[110];
    	                System.arraycopy(dataToEncrypt, i*110, data, 0, data.length);
    	                byte encData[] = cipher.doFinal(data);
    	                blocks.add(encData);
    	            }else{
                        byte data[] = new byte[dataToEncrypt.length-i*110];
                        System.arraycopy(dataToEncrypt, i*110, data, 0, data.length);
                        byte encData[] = cipher.doFinal(data);
                        blocks.add(encData);
    	            }
    	        }
    	        byte result[] = new byte[256*blocks.size()+1];
    	        result[0] = (byte)blocks.size();
    	        int pos = 1;
    	        for(byte[] d:blocks){
    	            System.arraycopy(d,0, result, pos, d.length);
    	            pos+=d.length;
    	        }
    	        return result;
    	    }
    	    
    	}catch(Exception e){
    		VLogger.error("Failed to encrypt RSA",e);
    	}
    	return null;
    }

    public static final byte[] decryptRSA(byte dataToDecrypt[],PrivateKey privateKey){
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            if(dataToDecrypt[0]==0){
                byte data[] = new byte[dataToDecrypt.length-1];
                System.arraycopy(dataToDecrypt, 1, data, 0, data.length);
                return cipher.doFinal(data);
            }else{
                int blockCount = dataToDecrypt[0];
                List<byte[]> data = new ArrayList<byte[]>(blockCount);
                int len = 0;
                for(int i=0;i<blockCount;i++){
                    byte encData[] = new byte[256];
                    System.arraycopy(dataToDecrypt, 256*i+1, encData,0, 256);
                    byte decData[] = cipher.doFinal(encData);
                    len+=decData.length;
                    data.add(decData);
                }
                byte result[] = new byte[len];
                int pos = 0;
                for(byte d[]:data){
                    System.arraycopy(d, 0, result,pos, d.length);
                    pos+=d.length;
                }
                return result;
            }
        }catch(Exception err){
            VLogger.error("Failed to decrypt RSA",err);
        }
        return null;
    }

    public static byte[] getRSAPublicKey(PublicKey key){
        try{
        	return key.getEncoded();
        }catch(Exception e){
        	e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getRSAPublicKey(byte[] data){
    	try {
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

    public static byte[] getSecretKey(SecretKey key){
        ObjectOutputStream bout = null;
        try{
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            bout = new ObjectOutputStream(new DataOutputStream(baout));
            bout.writeObject(key);
            bout.flush();
            return baout.toByteArray();
        }catch(Exception e){
            
        }finally{
            if(bout!=null)
                try {
                    bout.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return null;
    }

    public static SecretKey getSecretKey(byte[] data){
        ObjectInputStream in = null;
        try{
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            in = new ObjectInputStream(new DataInputStream(bin));
            return (SecretKey)in.readObject();
        }catch(Exception e){
            
        }finally{
            if(in!=null)
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return null;
    }
}
