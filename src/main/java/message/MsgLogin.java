package message;

public class MsgLogin extends Message {
    private String username;
    private String password;

    public MsgLogin(String username, String password) {
        super(Type.LOGIN);
        this. username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
