package message;

import java.io.Serializable;

public class Message implements Serializable{
    public enum Type {
        OK, PING, REPLY, LOGIN, LIST, ADDFILE, CHUNK, GETFILE, ERROR
    }

    private Type type;

    public Message () {
        this(Type.PING);
    }

    public Message (Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message type: " + type.toString();
    }
}
