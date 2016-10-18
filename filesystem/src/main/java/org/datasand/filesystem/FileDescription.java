package org.datasand.filesystem;

/**
 * Created by saichler on 10/18/16.
 */
public class FileDescription {
    private String name = null;
    private long lastChanged = -1;
    private long length = -1;
    private int version = -1;
    private long md5A = -1;
    private long md5B = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(long lastChanged) {
        this.lastChanged = lastChanged;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getMd5A() {
        return md5A;
    }

    public void setMd5A(long md5A) {
        this.md5A = md5A;
    }

    public long getMd5B() {
        return md5B;
    }

    public void setMd5B(long md5B) {
        this.md5B = md5B;
    }
}
