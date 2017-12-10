package files;

import message.MsgAddFile;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileHandler {
    private String usersPath;
    private int buffSize = 4;

    public FileHandler(String usersPath) {
        this.usersPath = usersPath;
    }

    public void addFile(MsgAddFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPath().toString());
        System.out.println(path.toAbsolutePath().toString());
    }

    public byte[] filePart(String pathstr, int part) {
        try {
            Path path = Paths.get(pathstr);
            //System.out.println(path.toAbsolutePath().toString());
            RandomAccessFile file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            if (part * buffSize >= file.length()) {
                file.close();
                return null;
            }
            file.seek(part * buffSize);
            byte[] buff = new byte[buffSize];
            int n = file.read(buff);
            byte[] slice = Arrays.copyOfRange(buff, 0, n);
            file.close();
            return slice;
            /*while ((n = file.read(buff)) != -1) {
                byte[] slice = Arrays.copyOfRange(buff, 0, n);
                System.out.print(new String(slice, Charset.forName("UTF-8")));
                file2.write(slice);
            }*/
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    public void fileAppend(String pathstr, byte[] buff) {
        try {
            Path path = Paths.get(pathstr);
            RandomAccessFile file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            file.write(buff);
            file.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
