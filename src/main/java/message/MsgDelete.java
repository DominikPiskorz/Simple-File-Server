package message;

public class MsgDelete extends Message{
    private String path;
    private String date;
    private String user;

    public MsgDelete(String path, String date, String user){
        super(Message.Type.DELETE);
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
        return super.toString() + " User: " + user + " File: " + path + " Date: " + date;
    }
}