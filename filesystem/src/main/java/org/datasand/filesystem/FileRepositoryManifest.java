package org.datasand.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;

public class FileRepositoryManifest {

    private final String name;
    private final String directory;
    private final Set<String> fileExtentions = new HashSet<>();
    private final Map<String, FileDescription> filesInRepository = new HashMap<>();

    public FileRepositoryManifest(String name, String directory) {
        this.name = name;
        this.directory = directory;
        File dir = new File(directory);
        if(!dir.exists()){
            throw new IllegalArgumentException("Directoery "+directory+" relative to:"+System.getProperty("user.dir")+" does not exist!");
        }
        collectFiles(dir);
    }

    public List<FileDescription> compareToOther(FileRepositoryManifest other) {
        List<FileDescription> result = new ArrayList<>();
        for (FileDescription fd : other.filesInRepository.values()) {
            FileDescription myFile = this.filesInRepository.get(fd.getRelativePath());
            if(myFile==null) {
                result.add(fd);
            }
        }
        return result;
    }

    public String getName(){
        return this.name;
    }

    public String getDirectory() {
        return this.directory;
    }

    public int getSize(){
        return this.filesInRepository.size();
    }

    public FileDescription getFileDescription(String relativePath){
        return this.filesInRepository.get(relativePath);
    }

    protected void addFileDescription(File f) {
        if (filesInRepository.containsKey(f.getPath().substring(this.directory.length())))
            return;

        if (!fileExtentions.isEmpty() && !fileExtentions.contains("*")) {
            int i = f.getName().lastIndexOf(".");
            if (i == -1)
                return;
            String ext = f.getName().substring(i + 1).trim();
            if (!fileExtentions.contains(ext)) {
                return;
            }
        }

        long md5[] = getFileMD5(f);
        FileDescription fd = new FileDescription(this.name,f.getPath().substring(this.directory.length()),f.lastModified(),f.length(),0,md5[0],md5[1]);
        filesInRepository.put(fd.getRelativePath(), fd);
    }

    public void collectFiles() {
        collectFiles(new File(this.directory));
    }

    private void collectFiles(File dir) {
        File files[] = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    collectFiles(f);
                } else {
                    addFileDescription(f);
                }
            }
        }
    }

    public static void encode(Object obj, BytesArray ba) {
        FileRepositoryManifest rm = (FileRepositoryManifest) obj;
        Encoder.encodeString(rm.name, ba);
        Encoder.encodeString(rm.directory, ba);
        Encoder.encodeInt32(rm.fileExtentions.size(), ba);
        for (String ext : rm.fileExtentions) {
            Encoder.encodeString(ext, ba);
        }
        Encoder.encodeInt32(rm.filesInRepository.size(), ba);
        for (FileDescription fd : rm.filesInRepository.values()) {
            FileDescription.SERIALIZER.encode(fd,ba);
        }
    }

    public static Object decode(BytesArray ba) {
        String name = Encoder.decodeString(ba);
        String directory = Encoder.decodeString(ba);

        FileRepositoryManifest rm = new FileRepositoryManifest(name, directory);

        int size = Encoder.decodeInt32(ba);
        for (int i = 0; i < size; i++) {
            rm.fileExtentions.add(Encoder.decodeString(ba));
        }
        size = Encoder.decodeInt32(ba);
        for (int i = 0; i < size; i++) {
            FileDescription fd = (FileDescription)FileDescription.SERIALIZER.decode(ba);
            rm.filesInRepository.put(fd.getRelativePath(), fd);
        }
        return rm;
    }

    public static long[] getFileMD5(File f) {
        try {
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[1024 * 1024];
                MessageDigest complete = MessageDigest.getInstance("MD5");
                int numRead;
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        complete.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);
                fis.close();
                byte by[] = complete.digest();
                long a = 0;
                a = (a << 8) + (by[0] & 0xff);
                a = (a << 8) + (by[1] & 0xff);
                a = (a << 8) + (by[2] & 0xff);
                a = (a << 8) + (by[3] & 0xff);
                a = (a << 8) + (by[4] & 0xff);
                a = (a << 8) + (by[5] & 0xff);
                a = (a << 8) + (by[6] & 0xff);
                a = (a << 8) + (by[7] & 0xff);

                long b = 0;
                b = (b << 8) + (by[8] & 0xff);
                b = (b << 8) + (by[9] & 0xff);
                b = (b << 8) + (by[10] & 0xff);
                b = (b << 8) + (by[11] & 0xff);
                b = (b << 8) + (by[12] & 0xff);
                b = (b << 8) + (by[13] & 0xff);
                b = (b << 8) + (by[14] & 0xff);
                b = (b << 8) + (by[15] & 0xff);
                return new long[]{a, b};
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
