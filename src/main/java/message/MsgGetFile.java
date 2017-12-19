package message;

public class MsgGetFile extends Message{
    private String path;
    private String date;
    private String user;

    public MsgGetFile(String path, String date, String user){
        super(Message.Type.GETFILE);
        this.path = path;
        this.date = date;
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getPathDate() {
        return path + date;
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user + " File: " + path + " Date: " +date;
    }
}
