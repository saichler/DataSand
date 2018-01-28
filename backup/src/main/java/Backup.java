import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Backup extends Thread {
    private static long MAX_LOAD = 20971520L;
    private boolean isrunning = true;
    private final List<BackupDir> toBackupList = new ArrayList<>();

    public static void main(String[] args) {
        new Backup();
    }

    private static class BackupDir {
        private final String source;
        private final String dest;
        public BackupDir(String source, String dest){
            this.source = source;
            this.dest = dest;
        }
    }

    public Backup() {
        super("Backup Thread");
        loadConfig();
        this.start();

        try {
            while(true) {
                String cmd;
                do {
                    System.out.print("CMD>");
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    cmd = in.readLine();
                } while(!cmd.contains("exit"));

                System.out.println("Stopping Gracefully");
                this.isrunning = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(){
        try{
            File f = new File("./backup.ini");
            Properties p = new Properties();
            if(f.exists()) {
                FileInputStream in = new FileInputStream(f);
                p.load(in);
                in.close();
                for (Map.Entry e : p.entrySet()) {
                    String source = (String) e.getKey();
                    String dest = (String) e.getValue();
                    BackupDir b = new BackupDir(source, dest);
                    toBackupList.add(b);
                }
            } else {
                p.put("Z:\\Backups","C:\\Users\\saichler\\Dropbox\\Backups");
                FileOutputStream out = new FileOutputStream(f);
                p.store(out,"backup config");
                out.close();
                loadConfig();
            }
        }catch(Exception err){

        }
    }

    public void run() {
        try {
            for(BackupDir dir:this.toBackupList) {
                this.doBackup(new File(dir.source), new File(dir.dest));
            }
            System.out.println("Done!");
            System.exit(0);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private String getFileSizeInMB(long size) {
        long result = size / 1024L / 1024L;
        return result + "m";
    }

    public void doBackup(File from, File to) throws IOException {
        if (!to.exists()) {
            System.out.println("Creating destination dir " + to.getAbsolutePath());
            to.mkdirs();
        } else {
            System.out.println("Scanning " + to.getAbsolutePath());
        }

        File[] fromFiles = from.listFiles();
        File[] var7 = fromFiles;
        int var6 = fromFiles.length;

        for(int var5 = 0; var5 < var6; ++var5) {
            File file = var7[var5];
            if (!this.isrunning) {
                System.exit(0);
            }

            File dest;
            if (file.isDirectory()) {
                dest = new File(to.getPath() + "/" + file.getName());
                this.doBackup(file, dest);
            } else {
                dest = new File(to.getPath() + "/" + file.getName());
                if (!dest.exists()) {
                    this.copyFile(file, dest);
                } else if (file.length() != dest.length()) {
                    System.out.println("File Size Error " + dest.getAbsolutePath());
                    this.copyFile(file, dest);
                }
            }
        }

    }

    public void copyFile(File file, File dest) throws IOException {
        if (!this.isrunning) {
            System.exit(0);
        }
        long fileSize = file.length();
        System.out.print("Copying File " + file.getAbsolutePath() + " (Size=" + this.getFileSizeInMB(fileSize) + ")");
        int pieces = (int)(fileSize / MAX_LOAD);
        long lastPiece = fileSize % MAX_LOAD;
        FileInputStream source = new FileInputStream(file);
        FileOutputStream target = new FileOutputStream(dest);

        for(int i = 0; i < pieces; ++i) {
            if (i % 10 == 0) {
                System.out.print(i * 20 + "m");
            }

            byte[] data = new byte[(int)MAX_LOAD];
            source.read(data);
            target.write(data);
            if (i % 2 == 0) {
                System.out.print(".");
            }
        }

        byte[] lastData = new byte[(int)lastPiece];
        source.read(lastData);
        target.write(lastData);
        source.close();
        target.close();
        System.out.println("Done.");
    }
}
