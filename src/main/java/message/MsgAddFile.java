package message;

public class MsgAddFile extends Message {
    private String path;
    private String user;
    private boolean verHis;

    public MsgAddFile(String path, String user){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.verHis = false;
    }

    public MsgAddFile(String path, String user, boolean verHis){
        super(Type.ADDFILE);
        this.path = path;
        this.user = user;
        this.verHis = verHis;
    }

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    public boolean isVerHis() {
        return verHis;
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user + " File: " + path;
    }
}
