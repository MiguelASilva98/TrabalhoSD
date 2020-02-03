import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestHandler extends UnicastRemoteObject implements FrontEndInterface {

    private List<ServerNode> nodeList = new ArrayList<>(); // array de placeManagers

    public RequestHandler() throws RemoteException {
    }

    /**
     * obtem um nodo aleatório
     * @return - node aleatório
     */
    public ServerNode getRandomNode() {
        int nodeCounter = nodeList.size() - 1;


        if (nodeCounter > 0) {
            Random random = new Random();
            int nodeIndex = random.nextInt(nodeCounter + 1); //gerar numeros aleatórios

            return nodeList.get(nodeIndex);
        }

        return null;
    }

    /**
     * Faz reset à flag isLeader de todos os node presentes na lista
     */
    private void resetLeader() {
        if (nodeList != null && nodeList.size() > 0 ) {
            for (ServerNode node : nodeList) {
                node.setLeader(false);
            }
        }
    }

    /**
     * Define um node como Leader
     * @param port - id do node a definir como lider
     * @throws RemoteException
     */
    @Override
    public void setLeader(int port) throws RemoteException {
        resetLeader();
        if (nodeList != null && nodeList.size() > 0 ) {

            for (ServerNode node : nodeList) {
                if (node.getId() == port) {
                    node.setLeader(true);
                }
            }
        }

    }

    /**
     * retorna o node correspondente ao lider
     * @return - node lider
     * @throws RemoteException
     */
    @Override
    public ServerNode getLeaderNode() throws RemoteException {
        for (ServerNode node : nodeList) {
            if (node.isLeader()) {
                return node;
            }
        }
        return null;
    }

    /**
     * adiciona um place invocando o addPlace do node lider
     * @param p - place a adicionar
     * @throws RemoteException
     */
    @Override
    public boolean addPlace(Place p) throws RemoteException {
        if (p != null) {

            ServerNode leaderNode = getLeaderNode(); //obtem o node lider

            try {
                PlacesListInterface pm = (PlacesListInterface) Naming.lookup(leaderNode.getRmiAddress()); //obter o placeManager lider

                return pm.addPlace(p);

            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * adiciona um nodo à lista de nodos conhecidos e inicia o respetivo placeManager
     * @param nodePort - nodo a ser iniciado
     * @throws RemoteException
     */
    @Override
    public void addNode(int nodePort) throws RemoteException {

        ServerNode node = new ServerNode(nodePort, "rmi://localhost:" + nodePort + "/placelist");

        if (!nodeList.contains(node)) {
            RMIServer.main(new String[]{String.valueOf(nodePort)});
            nodeList.add(node);
        }
    }

    /**
     * Obtem a lista de places de um nodo aleatório
     * @return lista de todos os places
     * @throws RemoteException
     */
    @Override
    public ArrayList allPlaces() throws RemoteException {
        ServerNode node = getRandomNode(); //devolve um node aleatorio
        try {
            PlacesListInterface pm = (PlacesListInterface) Naming.lookup(node.getRmiAddress()); //obtem o place manager por RMI
            return pm.allPlaces(); //devolve a lista de todos os places do placeManager

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtem a informação de um place em especifico, obtido de forma aleatoria
     * @param cp - codigo postal do place a obter
     * @return place pretendido
     * @throws RemoteException
     */
    @Override
    public Place getPlace(String cp) throws RemoteException {
        ServerNode node = getRandomNode(); //obtem um nodo aleatorio
        try {
            PlacesListInterface pm = (PlacesListInterface) Naming.lookup(node.getRmiAddress()); //obtem a instancia do placeManager
            return pm.getPlace(cp); //obtem a informação do place correspondente ao codigo postal

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int getLeader() throws RemoteException {
        return getLeaderNode().getId();
    }

    @Override
    public boolean removePlace(Place place) throws RemoteException {
        if (place != null) {

            ServerNode leaderNode = getLeaderNode(); //obtem o node lider

            try {
                PlacesListInterface pm = (PlacesListInterface) Naming.lookup(leaderNode.getRmiAddress()); //obter o placeManager lider

                return pm.removePlace(place);

            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}
