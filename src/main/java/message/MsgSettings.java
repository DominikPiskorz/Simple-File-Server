package message;

public class MsgSettings extends Message {
    private int partSize;

    public MsgSettings(int partSize) {
        super(Type.SETTINGS);
        this.partSize = partSize;
    }

    public int getPartSize() {
        return partSize;
    }
}
