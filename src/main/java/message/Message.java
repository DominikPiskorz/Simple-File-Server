package message;

import java.io.Serializable;

/**
 * Abstract class for network protocol.
 * All sent mesages should be objects inheriting from it.
 */
public abstract class Message implements Serializable{
    public enum Type {
        OK, PING, REPLY, LOGIN, LIST, ADDFILE, CHUNK, GETFILE, GETFILEVER, FILEVER, ERROR, SETTINGS, EXIT, DELETE
    }

    private Type type;

    protected Message (Type type) {
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
