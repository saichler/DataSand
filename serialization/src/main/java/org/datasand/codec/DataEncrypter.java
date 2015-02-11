package org.datasand.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DataEncrypter {

    private static SecretKey key = null;
    private static final String ENCRYPTED_TAG = "Encrypted:";
    static {
        init();
    }

    private static final void init() {
        if (key == null) {
            try {
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream(".aesenc"));
                key = (SecretKey) in.readObject();
                in.close();
            } catch (Exception err) {
            }
        }

        if (key == null) {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128);
                key = keyGen.generateKey();
                ObjectOutputStream out = new ObjectOutputStream(
                        new FileOutputStream(".aesenc"));
                out.writeObject(key);
                out.close();
            } catch (Exception err) {
            }
        }
    }

    public static String encrypt(String str) {
        if (str.startsWith(ENCRYPTED_TAG)) {
            return str;
        }
        try {
            Cipher cr = Cipher.getInstance("AES");
            cr.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            CipherOutputStream out = new CipherOutputStream(bout, cr);
            byte[] data = str.getBytes();
            out.write(data);
            out.close();
            byte[] encData = bout.toByteArray();
            return ENCRYPTED_TAG + DatatypeConverter.printBase64Binary(encData);
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encStr) {
        if (!encStr.startsWith(ENCRYPTED_TAG)) {
            return encStr;
        }
        try {
            Cipher cr = Cipher.getInstance("AES");
            cr.init(Cipher.DECRYPT_MODE, key);
            byte encData[] = DatatypeConverter.parseBase64Binary(encStr
                    .substring(ENCRYPTED_TAG.length()));
            ByteArrayInputStream bin = new ByteArrayInputStream(encData);
            CipherInputStream in = new CipherInputStream(bin, cr);
            byte data[] = new byte[encStr.length()];
            in.read(data);
            in.close();
            return new String(data).trim();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
