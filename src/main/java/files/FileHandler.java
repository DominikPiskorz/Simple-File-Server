package files;

import message.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Class for file-based operations - saving, splitting into messages, deleting.
 */
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

    /**
     * Main loop - wait for command then execute it, pass the result to ServerHandler.
     */
    public void run() {
        try {
            while (true) {
                System.out.println("FileHandler Waiting.");
                Message msg = inQueue.take();
                switch (msg.getType()) {
                    case ADDFILE:
                        outQueue.put(add((MsgAddFile) msg));
                        continue;
                    case GETFILE:
                        outQueue.put(send((MsgGetFile) msg));
                        continue;
                    case GETFILEVER:
                        outQueue.put(fileVer((MsgGetFileVer) msg));
                        continue;
                    case LIST:
                        outQueue.put(sendList(((MsgList) msg).getUser()));
                        continue;
                    case DELETE:
                        outQueue.put(deleteFile(((MsgDelete) msg).getPath(), ((MsgDelete) msg).getDate(), ((MsgDelete) msg).getUser()));
                        continue;
                    case EXIT:
                        break;
                    default:
                        continue;
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Download and save new file from client. Add file to list of files if it isn't there.
     * Data is saved in temp file until download is complete, so in case of crash
     * the unfinished file isn't created.
     * @return OK message if succesful, Error message otherwise
     */
    private Message add(MsgAddFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPath() + msg.getDateString());
        Path tempPath = Paths.get(usersPath, msg.getUser(), msg.getPath() + msg.getDateString() + ".temp");
        System.out.println("Adding file: " + path.toString());
        System.out.println(path.toAbsolutePath().toString() + msg.getDateString());
        int parts = (int) (msg.getFileSize() + partSize - 1) / partSize;
        RandomAccessFile file = null;
        try {
            // Utworz folder, jesli nie istnieje
            File targetFile = new File(path.toAbsolutePath().toString());
            if (targetFile.exists()) {
                Files.delete(targetFile.toPath());
            }

            File parent = targetFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }

            file = new RandomAccessFile(tempPath.toAbsolutePath().toString(), "rw");
            for (int currPart = 0; currPart < parts; currPart++) {
                MsgFileChunk chunk = (MsgFileChunk) inQueue.take();
                file.seek(currPart * partSize);
                file.write(chunk.getData());
            }
            file.close();
            Files.move(tempPath, path, REPLACE_EXISTING);
            registerFile(msg.getPath(), msg.getUser(), msg.isVerHis());
            return new MsgOk();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new MsgError(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            return new MsgError(e.toString());
        }
    }

    /**
     * Send file to client. Split file into chunks and pass them to ServerHandler.
     * @return OK message if succesful, Error message otherwise
     */
    private Message send(MsgGetFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPathDate());
        System.out.println("Sending file: " + path.toString());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            int parts = (int) (file.length() + partSize - 1) / partSize;
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

    /**
     * Check what versions of file are saved on server.
     * Version number is the same as the edit date.
     * @return Message containing string with all file versions (dates). String is empty
     * if file doesn't exist.
     */
    private MsgFileVer fileVer(MsgGetFileVer msg) {
        String msgPath = msg.getPath();
        // Swap "\" to "/" to avoid Unix/Windows file paths compatibility issues
        msgPath = msgPath.replace("\\", "/");
        String filename = msg.getPath();
        if (msgPath.contains("/")) {
            String[] parts = msgPath.split("/");
            filename = parts[parts.length - 1];
            msgPath = "";
            for (int i = 0; i < parts.length - 1; i++)
                msgPath = msgPath + (i > 0 ? "/" : "") + parts[i];
        } else
            msgPath = "";

        // Go through saved files and check if any of them is a version we're looking for
        Path path = Paths.get(usersPath, msg.getUser(), msgPath);
        Path filenamePath = Paths.get(usersPath, msg.getUser(), msgPath, filename);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filename + "*");
        List<String> list = new ArrayList<String>();
        File dir = new File(path.toString());
        File[] directoryListing = dir.listFiles();
        if (directoryListing == null)
            return new MsgFileVer("");
        for (File child : directoryListing) {
            Path childPath = child.toPath();
            if (childPath.getFileName().toString().length() > 19 && matcher.matches(childPath.getFileName())) {
                String substring = childPath.toString().substring(childPath.toString().length() - 19);
                substring = substring.replace("-", ":");
                substring = substring.replace("_", " ");
                substring = substring.replace(".", "/");
                list.add(substring);
            }
        }
        String dates = String.join(";",list);
        return new MsgFileVer(dates);

    }

    /**
     * Get a part of a file.
     * @param part Index of part
     * @return Data as a array of bytes
     */
    private byte[] getPart(RandomAccessFile file, int part) {
        try {
            if (part * partSize >= file.length()) {
                throw new IllegalArgumentException("Input argument part is too big.");
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


    /**
     * Add file to user's list of files if it's not there already.
     */
    private void registerFile(String path, String user, boolean hisVer) {
        path = path.replace("\\", "/");
        Path listPath = Paths.get(usersPath,user+".list");

        // Create folder if needed
        File fileList = new File(listPath.toAbsolutePath().toString());
        File parent = fileList.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        if (!fileList.exists()) {
            try {
                Writer output = new BufferedWriter(new FileWriter(listPath.toString(), true));
                output.append(path + ";" + (hisVer ? 1 : 0 + System.lineSeparator()));
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(listPath)) {
            String line;
            String input = "";
            boolean replace = false;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                input += line + System.lineSeparator();
                String parts[] = line.split(";");
                if (parts[0].equals(path)) {
                    found = true;
                    if (hisVer == Boolean.parseBoolean(parts[1]))
                        break;
                    else {
                        replace = true;
                        input = input.replace(line, parts[0] + ";" + (hisVer ? 1 : 0));
                    }
                }
            }

            if (replace){
                FileOutputStream os = new FileOutputStream(listPath.toString());
                os.write(input.getBytes());
                os.close();
            }
            else if (!found) {
                Writer output = new BufferedWriter(new FileWriter(listPath.toString(), true));
                output.append(path + ";" + (hisVer ? 1 : 0) + System.lineSeparator());
                output.close();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send user's list of files.
     * List is sent the same way as ordinary files are.
     * @return OK message if succesful, Error message otherwise
     */
    private Message sendList(String user) {
        Path path = Paths.get(usersPath,user + ".list");
        System.out.println("Sending list: " + path.toString());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
            int parts = (int) (file.length() + partSize - 1) / partSize;
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

    /**
     * Delete file and delete entry in user's file list if it's the only version.
     * @return OK message if succesful, Error message otherwise.
     */
    private Message deleteFile(String strPath, String date, String user) {
        strPath = strPath.substring(1);
        System.out.println("Deleting " + strPath);
        Path path = Paths.get(usersPath, user, strPath + date);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if any other versions remain.
        MsgFileVer versions = fileVer(new MsgGetFileVer(strPath, user));
        if (!versions.getDates().equals(""))
            return new MsgOk();

        //Last version was deleted so delete entry in user's file list.
        Path listPath = Paths.get(usersPath, user + ".list");
        Path tempPath = Paths.get(usersPath, user + ".list.temp");
        try {
            BufferedReader br = Files.newBufferedReader(listPath);
            BufferedWriter bw = Files.newBufferedWriter(tempPath);
            String line;
            while ((line = br.readLine()) != null) {
                String parts[] = line.split(";");
                if (parts[0].equals(strPath)) {
                    continue;
                }
                bw.write(line + System.lineSeparator());
            }
            bw.close();
            br.close();
            Files.move(tempPath, listPath, REPLACE_EXISTING);

            System.out.println("Deleted.");
            return new MsgOk();
        } catch (Exception e) {
            e.printStackTrace();
            return new MsgError(e.toString());
        }

    }
}
