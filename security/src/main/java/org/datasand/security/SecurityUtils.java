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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SecurityUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);
    private static byte[] iv = {0, 4, 0, 0, 6, 81, 0, 8, 71, 86, 5, 0, 0, 43, 0, 1};
    protected static IvParameterSpec ivspec = new IvParameterSpec(iv);

    //Encrypt a byte array using the given key.
    public static byte[] encrypt(byte[] dataToEncode, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        Cipher cr = Cipher.getInstance("AES/CFB8/NoPadding");
        cr.init(Cipher.ENCRYPT_MODE, key, ivspec);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CipherOutputStream out = new CipherOutputStream(bout, cr);
        out.write(dataToEncode);
        out.close();
        return bout.toByteArray();
    }

    //Decrypt a byte array using the given key
    public static byte[] decrypt(byte[] dataToDecrypt, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        Cipher cr = Cipher.getInstance("AES/CFB8/NoPadding");
        cr.init(Cipher.DECRYPT_MODE, key, ivspec);
        ByteArrayInputStream bin = new ByteArrayInputStream(dataToDecrypt);
        CipherInputStream in = new CipherInputStream(bin, cr);
        byte data[] = new byte[dataToDecrypt.length];
        in.read(data);
        in.close();
        return data;
    }

    public static final SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    public static final KeyPair generateRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.genKeyPair();
    }

    public static final byte[] encryptRSA(byte dataToEncrypt[], PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //if the data is larger than 117 bytes. we need to encrypt it in two blocks
        //we divide to 110 per block
        byte blockCount = 0;
        if (dataToEncrypt.length > 110) {
            blockCount = (byte) (dataToEncrypt.length / 110 + 1);
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        if (blockCount == 0) {
            byte encData[] = cipher.doFinal(dataToEncrypt);
            byte[] result = new byte[encData.length + 1];
            result[0] = (byte) encData.length;
            System.arraycopy(encData, 0, result, 1, encData.length);
            return result;
        } else {
            List<byte[]> blocks = new LinkedList<>();
            for (int i = 0; i < blockCount; i++) {
                if (dataToEncrypt.length - i * 110 >= 110) {
                    byte data[] = new byte[110];
                    System.arraycopy(dataToEncrypt, i * 110, data, 0, data.length);
                    byte encData[] = cipher.doFinal(data);
                    blocks.add(encData);
                } else {
                    byte data[] = new byte[dataToEncrypt.length - i * 110];
                    System.arraycopy(dataToEncrypt, i * 110, data, 0, data.length);
                    byte encData[] = cipher.doFinal(data);
                    blocks.add(encData);
                }
            }
            byte result[] = new byte[256 * blocks.size() + 1];
            result[0] = (byte) blocks.size();
            int pos = 1;
            for (byte[] d : blocks) {
                System.arraycopy(d, 0, result, pos, d.length);
                pos += d.length;
            }
            return result;
        }
    }

    public static final byte[] decryptRSA(byte dataToDecrypt[], PrivateKey privateKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        if (dataToDecrypt[0] == 0) {
            byte data[] = new byte[dataToDecrypt.length - 1];
            System.arraycopy(dataToDecrypt, 1, data, 0, data.length);
            return cipher.doFinal(data);
        } else {
            int blockCount = dataToDecrypt[0];
            List<byte[]> data = new ArrayList<byte[]>(blockCount);
            int len = 0;
            for (int i = 0; i < blockCount; i++) {
                byte encData[] = new byte[256];
                System.arraycopy(dataToDecrypt, 256 * i + 1, encData, 0, 256);
                byte decData[] = cipher.doFinal(encData);
                len += decData.length;
                data.add(decData);
            }
            byte result[] = new byte[len];
            int pos = 0;
            for (byte d[] : data) {
                System.arraycopy(d, 0, result, pos, d.length);
                pos += d.length;
            }
            return result;
        }
    }

    public static byte[] getRSAPublicKey(PublicKey key) {
        return key.getEncoded();
    }

    public static PublicKey getRSAPublicKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data));
    }

    public static byte[] getSecretKeyBytes(SecretKey key) throws IOException {
        ObjectOutputStream bout = null;
        try{
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            bout = new ObjectOutputStream(new DataOutputStream(baout));
            bout.writeObject(key);
            bout.flush();
            return baout.toByteArray();
        } finally {
            if (bout != null){
                bout.close();
            }
        }
    }

    public static SecretKey getSecretKeyFromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream in = null;
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            in = new ObjectInputStream(new DataInputStream(bin));
            return (SecretKey) in.readObject();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static final void saveKeyToFile(SecretKey key,String filename) throws IOException {
        File file = new File(filename);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(getSecretKeyBytes(key));
        }finally {
            out.close();
        }
    }

    public static final SecretKey loadKeyFromFile(String filename) throws IOException, ClassNotFoundException {
        FileInputStream in = null;
        File file = new File(filename);
        if(!file.exists()){
            throw new IllegalArgumentException("File "+filename+" does not exist");
        }
        try {
            in = new FileInputStream(filename);
            byte data[] = new byte[(int)file.length()];
            in.read(data);
            return getSecretKeyFromBytes(data);
        }finally {
            if(in!=null){
                in.close();
            }
        }
    }
}
