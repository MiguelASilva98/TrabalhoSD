import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class FrontEnd {
    private static final int PORT = 2000;

    public static void main(String[] args) {
        try {
            //regista o RequestHanler para se tornar acessivel por RMI através
            //de um endereço do tipo rmi://localhost:porta/frontend
            Registry registry = LocateRegistry.createRegistry(PORT);
            RequestHandler handler = new RequestHandler();
            registry.rebind("frontend", handler);

            handler.addNode(2028);
            handler.addNode(2029);
            handler.addNode(2030);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
