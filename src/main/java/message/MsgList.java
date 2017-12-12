package message;

public class MsgList extends Message{
    private String user;

    public MsgList(String user){
        super(Type.LIST);
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return super.toString() + " User: " + user;
    }
}