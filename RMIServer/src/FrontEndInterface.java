import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FrontEndInterface {
    void setLeader(int port) throws RemoteException;
    boolean removePlace(Place p) throws java.rmi.RemoteException;
}
