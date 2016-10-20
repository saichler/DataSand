package org.datasand.filesystem;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

/**
 * Created by saichler on 10/18/16.
 */
public class FileDescription implements ISerializer{

    public static final ISerializer SERIALIZER = new FileDescription(null,null,-1,-1,-1,-1,-1);
    static {
        Encoder.registerSerializer(FileDescription.class,SERIALIZER);
    }

    private final String repoName;
    private final String relativePath;
    private final long lastChanged;
    private final long length;
    private final int version;
    private final long md5A;
    private final long md5B;

    public FileDescription(String repoName,String relativePath,long lastChanged,long length, int version, long md5A, long md5B){
        this.repoName = repoName;
        this.relativePath = relativePath;
        this.lastChanged = lastChanged;
        this.length = length;
        this.version = version;
        this.md5A = md5A;
        this.md5B = md5B;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getLastChanged() {
        return lastChanged;
    }

    public long getLength() {
        return length;
    }

    public int getVersion() {
        return version;
    }

    public long getMd5A() {
        return md5A;
    }

    public long getMd5B() {
        return md5B;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        FileDescription fd = (FileDescription)value;
        Encoder.encodeString(fd.repoName,ba);
        Encoder.encodeString(fd.relativePath,ba);
        Encoder.encodeInt64(fd.lastChanged,ba);
        Encoder.encodeInt64(fd.length,ba);
        Encoder.encodeInt32(fd.version,ba);
        Encoder.encodeInt64(fd.md5A,ba);
        Encoder.encodeInt64(fd.md5B,ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        String repoName = Encoder.decodeString(ba);
        String relativePath = Encoder.decodeString(ba);
        long lastChanged = Encoder.decodeInt64(ba);
        long length = Encoder.decodeInt64(ba);
        int version = Encoder.decodeInt32(ba);
        long md5A = Encoder.decodeInt64(ba);
        long md5B = Encoder.decodeInt64(ba);
        return new FileDescription(repoName,relativePath,lastChanged,length,version,md5A,md5B);
    }
}
