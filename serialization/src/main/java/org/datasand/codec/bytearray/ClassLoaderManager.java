package org.datasand.codec.bytearray;

/**
 * Created by root on 1/7/16.
 */
public class ClassLoaderManager {
    public static ClassLoader getClassLoader(String str){
        return ClassLoaderManager.class.getClassLoader();
    }
}
