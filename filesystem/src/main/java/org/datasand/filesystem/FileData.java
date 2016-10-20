package org.datasand.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.Packet;

public class FileData {

    public static int MAX_PART_SIZE = Packet.MAX_DATA_IN_ONE_PACKET*20;
    private int part = -1;
    private final int total;
    private final String relativePath;
    private final String repoDirectory;
    private final String repoName;
    private byte[] data = null;

    public FileData(File f, String repoName, String repoDirectory) {
        this.repoName = repoName;
        this.repoDirectory = repoDirectory;
        this.relativePath = f.getPath().substring(repoDirectory.length());
        int t = (int) f.length() / MAX_PART_SIZE;
        if (t == 0) {
            t = 1;
        } else if (f.length() % MAX_PART_SIZE > 0) {
            t++;
        }
        this.total = t;
    }

    private FileData(String relativePath, String repoName, String repoDirectory, int part, int total, byte data[]) {
        this.relativePath = relativePath;
        this.repoName = repoName;
        this.repoDirectory = repoDirectory;
        this.part = part;
        this.total = total;
        this.data = data;
    }

    public boolean isFinishedSending() {
        if (total == part)
            return true;
        return false;
    }

    public boolean isFinishedRecieving() {
        if (total == part + 1)
            return true;
        return false;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public int getTotal() {
        return total;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getRepoDirectory() {
        return repoDirectory;
    }

    public String getRepoName() {
        return repoName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static void encode(Object value, BytesArray ba) throws IOException {
        FileData fileData = (FileData) value;

        if (fileData.part == fileData.total) return;

        if (fileData.part == -1) {
            Encoder.encodeString(fileData.relativePath, ba);
            Encoder.encodeString(fileData.repoName, ba);
            Encoder.encodeString(fileData.repoDirectory, ba);
            Encoder.encodeInt32(fileData.part, ba);
            Encoder.encodeInt32(fileData.total, ba);
        } else if (fileData.total == 1) {
            Encoder.encodeString(fileData.relativePath, ba);
            Encoder.encodeString(fileData.repoName, ba);
            Encoder.encodeString(fileData.repoDirectory, ba);
            Encoder.encodeInt32(fileData.part, ba);
            Encoder.encodeInt32(fileData.total, ba);
            FileInputStream in = null;
            try {
                File file = new File(fileData.repoDirectory + fileData.relativePath);
                in = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                in.read(data);
                in.close();
                Encoder.encodeByteArray(data, ba);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } else if (fileData.part < fileData.total - 1) {
            Encoder.encodeString(fileData.relativePath, ba);
            Encoder.encodeString(fileData.repoName, ba);
            Encoder.encodeString(fileData.repoDirectory, ba);
            Encoder.encodeInt32(fileData.part, ba);
            Encoder.encodeInt32(fileData.total, ba);
            File file = new File(fileData.repoDirectory + fileData.relativePath);
            RandomAccessFile r = null;
            try {
                byte data[] = new byte[MAX_PART_SIZE];
                r = new RandomAccessFile(file, "r");
                r.seek(fileData.part * MAX_PART_SIZE);
                r.read(data);
                r.close();
                Encoder.encodeByteArray(data, ba);
            } finally {
                r.close();
            }
        } else {
            Encoder.encodeString(fileData.relativePath, ba);
            Encoder.encodeString(fileData.repoName, ba);
            Encoder.encodeString(fileData.repoDirectory, ba);
            Encoder.encodeInt32(fileData.part, ba);
            Encoder.encodeInt32(fileData.total, ba);
            File file = new File(fileData.repoDirectory + fileData.relativePath);
            RandomAccessFile r = null;
            try {
                byte data[] = new byte[(int) (file.length() - fileData.part * MAX_PART_SIZE)];
                r = new RandomAccessFile(file, "r");
                r.seek(fileData.part * MAX_PART_SIZE);
                r.read(data);
                Encoder.encodeByteArray(data, ba);
            } finally {
                r.close();
            }
        }
    }

    public static Object decode(BytesArray ba) {
        String relativePath = Encoder.decodeString(ba);
        String repoName = Encoder.decodeString(ba);
        String repoDirectory = Encoder.decodeString(ba);
        int part = Encoder.decodeInt32(ba);
        int total = Encoder.decodeInt32(ba);
        FileData fd = null;
        if (part == -1) {
            fd = new FileData(relativePath, repoName, repoDirectory, part, total, null);
        } else {
            byte data[] = Encoder.decodeByteArray(ba);
            fd = new FileData(relativePath, repoName, repoDirectory, part, total, data);
        }
        return fd;
    }
}