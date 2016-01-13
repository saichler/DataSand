package org.datasand.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * Created by root on 1/12/16.
 */
public class DataFile {
    private final RandomAccessFile raf;

    public DataFile(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file,"rw");
    }

    public void write(byte[] data){

    }

}
