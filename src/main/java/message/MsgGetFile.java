package message;

public class MsgGetFile extends Message{
    private String path;
    private String user;

    public MsgGetFile(String path, String user){
        super(Message.Type.GETFILE);
        this.path = path;
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user + " File: " + path;
    }
}
