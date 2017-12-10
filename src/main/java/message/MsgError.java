package message;

public class MsgError extends Message {
    private String string;

    public MsgError(String string){
        super(Message.Type.ERROR);
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return super.toString() + " String: " + string;
    }
}
