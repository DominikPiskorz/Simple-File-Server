package message;

public class MsgDelete extends Message{
    private String path;
    private String user;

    public MsgDelete(String path, String user){
        super(Message.Type.DELETE);
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