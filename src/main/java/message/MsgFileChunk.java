package message;

import java.nio.charset.Charset;

public class MsgFileChunk extends Message {
    private byte[] data;
    private int fileSize;
    private int start;
    private int end;
    private int part;

    public MsgFileChunk(byte[] data, int part) {
        super(Type.CHUNK);
        this.data = data;
        this.part = part;
    }

    public MsgFileChunk(byte[] data, int fileSize, int start, int end) {
        super(Type.CHUNK);
        this.data = data;
        this.fileSize = fileSize;
        this.start = start;
        this.end = end;
    }

    public byte[] getData() {
        return data;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getPart() {
        return part;
    }

    @Override
    public String toString() {
        return super.toString() + " Part: " + part;// + " Data: " + new String(data, Charset.forName("UTF-8"));
    }
}
