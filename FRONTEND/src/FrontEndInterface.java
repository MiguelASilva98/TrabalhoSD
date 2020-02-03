import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FrontEndInterface  extends Remote {
  void setLeader(int port) throws RemoteException;
  int getLeader() throws RemoteException;
  ServerNode getLeaderNode() throws RemoteException;
  boolean addPlace(Place p) throws RemoteException;
  void addNode(int nodePort) throws RemoteException;
  ArrayList allPlaces() throws RemoteException;
  Place getPlace(String cp) throws RemoteException;
  boolean removePlace(Place place) throws RemoteException;
}
