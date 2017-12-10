package files;

import message.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class FileHandler implements Runnable {
    private BlockingQueue<Message> inQueue;
    private BlockingQueue<Message> outQueue;
    private String usersPath;
    private int partSize;

    public FileHandler(String usersPath, BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue, int partSize) {
        this.usersPath = usersPath;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.partSize = partSize;
    }

    public void run() {
        try {
            while (true) {
                System.out.println("Czekam");
                Message msg = inQueue.take();
                //System.out.println(msg.toString());
                switch (msg.getType()) {
                    case ADDFILE:
                        outQueue.put(add((MsgAddFile) msg));
                        break;
                    case GETFILE:
                        outQueue.put(send((MsgGetFile) msg));
                        break;
                    /*case CHUNK:
                    case LIST:
                    case REPLY:
                    case LOGIN:*/
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Message add(MsgAddFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPath());
        System.out.println("Dodaje plik: " + path.toString());
        System.out.println(path.toAbsolutePath().toString());
        int parts = (int) (msg.getFileSize() + partSize) / partSize;
        System.out.println(msg.getFileSize() + " " + partSize + " " + parts);
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            for (int currPart = 0; currPart < parts; currPart++) {
                MsgFileChunk chunk = (MsgFileChunk) inQueue.take();
                System.out.println("Dopisuje:" + msg.toString()  +
                        " part " + currPart +"/"+ parts + " pos: " + (currPart * partSize));
                //append(file, chunk.getData(), currPart);
                file.seek(currPart * partSize);
                file.write(chunk.getData());
            }
            return new MsgOk();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new MsgError(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            return new MsgError(e.toString());
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Message send(MsgGetFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPath());
        System.out.println("Wysylam plik: " + path.toString());
        System.out.println(path.toAbsolutePath().toString());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            int parts = (int) (file.length() + partSize) / partSize;
            for (int currPart = 0; currPart < parts; currPart++) {
                byte[] data = getPart(file, currPart);
                outQueue.put(new MsgFileChunk(data, currPart));
            }
            return new MsgOk();
        } catch (Exception e) {
            e.printStackTrace();
            return new MsgError(e.toString());
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] getPart(RandomAccessFile file, int part) {
        try {
            if (part * partSize >= file.length()) {
                throw new IllegalArgumentException("Argument part jest zbyt duzy.");
            }
            file.seek(part * partSize);
            byte[] buff = new byte[partSize];
            int n = file.read(buff);
            byte[] slice = Arrays.copyOfRange(buff, 0, n);
            return slice;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
