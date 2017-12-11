package message;

import java.util.Date;

public class MsgAddFile extends Message {
    private String path;
    private String user;
    private long fileSize;
    private boolean verHis;
    private Date lastedit;

    public MsgAddFile(String path, String user, long fileSize){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.fileSize = fileSize;
        this.verHis = false;
    }

    public MsgAddFile(String path, String user, long fileSize, boolean verHis){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.fileSize = fileSize;
        this.verHis = verHis;
    }

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isVerHis() {
        return verHis;
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user + " File: " + path;
    }
}
