import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServerInterface extends Remote{
    public void echo(String input) throws RemoteException;
    <T> T executeTask(Task<T> t) throws RemoteException;
}
