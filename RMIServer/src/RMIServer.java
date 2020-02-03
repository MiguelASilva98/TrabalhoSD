import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static java.lang.Thread.sleep;


public class RMIServer {

    public static void main(String[] args) {
        createPlaceManagers(Integer.valueOf(args[0]));
        //createPlaceManagers(2029);
    }

    private static void createPlaceManagers(int port) {
        try {
            //regista a instancia do placeManager para se tornar acessivel por RMI através
            //de um endereço do tipo rmi://localhost:porta/placelist

            Registry r = LocateRegistry.createRegistry(port);
            PlaceManager placeList = new PlaceManager(port);
            r.rebind("placelist", placeList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Place server main " + e.getMessage());
        }
    }
}