package files;

import message.MsgAddFile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    private String usersPath;

    public FileHandler(String usersPath) {
        this.usersPath = usersPath;
    }

    public void addFile(MsgAddFile msg) {
        Path path = Paths.get(usersPath, msg.getUser(), msg.getPath().toString());
        System.out.println(path.toAbsolutePath().toString());
    }
}
