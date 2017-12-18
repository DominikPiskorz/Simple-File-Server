package message;

public class MsgFileVer extends Message {
    private String dates;

    public MsgFileVer(String dates) {
        super(Type.FILEVER);
        this.dates = dates;
    }

    public String getDates() {
        return dates;
    }
}
