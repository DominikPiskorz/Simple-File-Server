package message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MsgAddFile extends Message {
    private String path;
    private String user;
    private long fileSize;
    private boolean verHis;
    private Date date;

    public MsgAddFile(String path, String user, long fileSize){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.fileSize = fileSize;
        this.verHis = false;
    }

    public MsgAddFile(String path, String user, long fileSize, Date date){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.fileSize = fileSize;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //19 characters
        return df.format(date);
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user + " File: " + path;
    }
}
