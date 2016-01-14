package org.datasand.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.VTable;

/**
 * Created by root on 1/12/16.
 */
public class DataFile {
    private final RandomAccessFile raf;
    private int currentIndex = 0;
    private int currentLocation = 0;
    private final VTable vTable;

    final Map<Integer,DataLocation> mainIndex = new HashMap<>();
    final Map<Integer,List<Integer>> parentToChildrenIndex = new HashMap<>();

    public DataFile(File file,VTable vTable) throws FileNotFoundException {
        this.vTable = vTable;
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        raf = new RandomAccessFile(file,"rw");
    }

    public int write(BytesArray key, HierarchyBytesArray obj,int parentIndex) throws IOException {
        byte data[] = obj.getData();
        DataLocation dl = new DataLocation(currentLocation,data.length,currentIndex,parentIndex);
        raf.write(data);
        return updateIndexes(dl,data.length,parentIndex);
    }

    private int updateIndexes(DataLocation dl,int dataLength,int parentIndex){
        mainIndex.put(currentIndex,dl);
        if(parentIndex!=-1) {
            List<Integer> childList = this.parentToChildrenIndex.get(parentIndex);
            if(childList==null){
                childList = new LinkedList<>();
                this.parentToChildrenIndex.put(parentIndex,childList);
            }
            childList.add(currentIndex);
        }
        int myIndex = currentIndex;
        currentIndex++;
        currentLocation+=dataLength;
        return myIndex;
    }

    public HierarchyBytesArray read(int index) throws IOException {
        DataLocation dl = this.mainIndex.get(index);
        raf.seek(dl.getStartPosition());
        byte data[] = new byte[dl.getLength()];
        raf.read(data);
        HierarchyBytesArray ba = new HierarchyBytesArray();
        ba.setBytesData(data);
        for(VTable child:this.vTable.getChildren()){
            DataFile childDf = DataFileManager.instance.getDataFile(Encoder.getMD5ByClass(child.getJavaClassType()));
            ba.getChildren().addAll(childDf.readChildren(index));
        }
        return ba;
    }

    public List<HierarchyBytesArray> readChildren(int parentIndex) throws IOException {
        List<Integer> children = this.parentToChildrenIndex.get(parentIndex);
        List<HierarchyBytesArray> result = new LinkedList<>();
        if(children!=null){
            for(Integer index:children) {
                result.add(read(index));
            }
        }
        return result;
    }
}