package org.datasand.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.HierarchyBytesArray;

/**
 * Created by root on 1/12/16.
 */
public class DataFile {
    private final RandomAccessFile raf;
    private int currentIndex = 0;
    private int currentLocation = 0;

    final Map<Integer,DataLocation> mainIndex = new HashMap<>();

    public DataFile(File file) throws FileNotFoundException {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        raf = new RandomAccessFile(file,"rw");
    }

    public void write(BytesArray key, HierarchyBytesArray obj) throws IOException {
        byte data[] = obj.getData();
        DataLocation dl = new DataLocation(currentLocation,data.length,currentIndex,-1);
        raf.write(data);
        mainIndex.put(currentIndex,dl);
        currentIndex++;
        currentLocation+=data.length;
    }

}
