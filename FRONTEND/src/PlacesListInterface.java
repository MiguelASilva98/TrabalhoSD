import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlacesListInterface extends Remote {
    boolean addPlace(Place p) throws RemoteException;
    boolean removePlace(Place place) throws RemoteException;
   // void addOperation(OperationLog log) throws  RemoteException;
    ArrayList allPlaces() throws RemoteException;
    Place getPlace(String id) throws RemoteException;
   // void stopHeartbeat() throws RemoteException;
}
