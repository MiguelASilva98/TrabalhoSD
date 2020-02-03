import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface {

    private int port; //porta usada pelo placeManager
    private int leaderId;
    private String frontendRMIAddress = "rmi://localhost:2000/frontend";
    private final int MULTICAST_PORT = 2222;
    private final String MULTICAST_ADDR = "225.0.0.0";
    private ArrayList<Place> placesList; //lista de places
    private HashMap<Integer, Long> pmMap; // mapa dos placeManagers conhecidos com a estrutura: [porto, timestamp]
    private ArrayList<OperationLog> logs; //registo de operaçoes executadas ou por executar do placeManager
    private boolean execute = true;
    Thread receiveThread; //thread responsavel por receber mensagens multicast
    Thread sendThread; //thread responsavel por enviar mensagens multicast
    Thread heartbeatThread; //thread responsavel por envio e processamento de heartbeat

    public PlaceManager(int port) throws RemoteException {
        this.port = port;
        this.leaderId = port;
        this.placesList = new ArrayList<>();
        this.pmMap = new HashMap<>();
        this.logs = new ArrayList<>();

        pmMap.put(port, System.currentTimeMillis()); //adiciona-se a ele proprio ao mapa de placeManagers conhecidos
        updateFrontendLeaderInfo(port); //alterar o lider no frontend por RMI

        this.receiveThread = new Thread() {
            @Override
            public void run() {
                receiveMessages();
            }
        };

        this.receiveThread.start();

        this.heartbeatThread = new Thread() {
            @Override
            public void run() {
                while (execute) {
                    try {
                        //verifica se é o lider e envia um heartbeat
                        if (port == leaderId) {
                            sleep(700);
                            sendHearbeat();
                        } else {
                            //se não for o lider
                            sleep(500);

                            sendMessage("alive," + port); //envio do "alive"
                        }
                        updatePlaceManagersListOnHeartbeatEvent(System.currentTimeMillis());//atualizar o mapa de placeManagers conhecidos com base no ultimo "alive" recebido
                    } catch (InterruptedException e) {

                    }
                }
            }
        };

        heartbeatThread.start();
        updateFollowers(); //a função inicializa uma thread responsavel por processar as operaçoes pendentes na lista de logs
    }

    private void setLeaderId(int leader) {
        this.leaderId = leader;
    }



    /**
     * Atualizar o leaderID no frontend por RMI
     */
    private void updateFrontendLeaderInfo(int leader) {
        try {
            FrontEndInterface frontEndInterface = (FrontEndInterface) Naming.lookup(frontendRMIAddress); //obter a instancia do frontend por RMI
            frontEndInterface.setLeader(leader);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * função responsavel por receber as mensagens do multicast
     */
    private void receiveMessages() {

        try {
            InetAddress address = InetAddress.getByName(MULTICAST_ADDR); // endereço de multicast
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT); // associa um socket ao porto do multicast
            socket.joinGroup(address); // junta o socket ao grupo de multicast

            while (execute) {
                byte[] msgBytes = new byte[1024];

                DatagramPacket msgPacket = new DatagramPacket(msgBytes, msgBytes.length);
                socket.receive(msgPacket); // recebe o datagrama da mensagem

                String msg = new String(msgPacket.getData(), msgPacket.getOffset(), msgPacket.getLength()); // converte a mensagem para string
                handleMessageReceived(msg); // processa a mensagem recebida
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * função responsavel por enviar as mensagens do multicast
     */
    private void sendMessage(String message) {
        this.sendThread = new Thread() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(MULTICAST_ADDR); // endereço de multicast
                    MulticastSocket socket = new MulticastSocket(MULTICAST_PORT); // associa um socket ao porto do multicast

                    DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, address, MULTICAST_PORT);
                    socket.send(msgPacket); // envia o datagrama com a mensagem pelo socket para o grupo de multicast
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        this.sendThread.start();
    }

    /**
     * função responsável por processar as mensagens multicast recebidas
     */
    private void handleMessageReceived(String msg) {
        if (msg != null && !msg.isEmpty()) {
            String[] msgArray = msg.split(","); //Cria um array com os elementos que estavam separados por ","
            switch (msgArray[0]) {
                //verificar o tipo de mensagem
                case "heartbeat": {
                    int leader = Integer.valueOf(msgArray[1]);
                    updatePlaceManagersListOnAliveEvent(System.currentTimeMillis(), leader); //atualiza/insere o timestamp para o porto recebido (lider)
                    //se recebeu um heartbeat de um lider diferente e com id menor que o atual
                    if (leader != leaderId && leader < leaderId) {
                        System.out.println(port + ": My leader is " + leader + " now");
                        setLeaderId(leader);
                        updateFrontendLeaderInfo(leader); //atualiza a informação do lider no frontend por RMI
                    }
                    break;
                }
                case "alive": {
                    int receivedPort = Integer.valueOf(msgArray[1]);
                    if (receivedPort != port) {
                        updatePlaceManagersListOnAliveEvent(System.currentTimeMillis(), receivedPort); //atualiza/insere o timestamp para o porto recebido
                    }
                    break;
                }
            }
        }
    }


    /**
     * Insere o porto recebido e o respetivo timestamp no mapa de portos conhecidos
     * ou atualiza apenas o timestamp caso o porto já seja conhecido
    */
    private synchronized void updatePlaceManagersListOnAliveEvent(long timestamp, int receivedPort) {
        if (!pmMap.containsKey(receivedPort)) {
            pmMap.put(receivedPort, timestamp);

            if (leaderId == port) {
                // quando o lider recebe um novo node, tem de o sincronizar
                syncNewNode(receivedPort);
            }
        } else {
            pmMap.replace(receivedPort, timestamp);
        }
    }

    /**
     * atualiza o mapa de placeManagers conhecidos tendo em conta a ultima mensagem recebida
     */
    private synchronized void updatePlaceManagersListOnHeartbeatEvent(long timestamp) {
        if (pmMap != null) {
            ArrayList<Integer> itemsToBeRemoved = new ArrayList<>();
            for (int currPort : pmMap.keySet()) {
                if (port != currPort) { // nunca se atualiza a ele proprio
                    long currTimestamp = pmMap.get(currPort);
                    //se passou mais que um segundo desde a ultima mensagem recebida remove do mapa
                    if (timestamp - currTimestamp > 1000) {
                        itemsToBeRemoved.add(currPort); //guarda-se o indice numa lista

                        if (currPort == leaderId) {
                            leaderId = 0;
                        }
                    }
                }
            }

            Collections.reverse(itemsToBeRemoved);

            //remover os indices guardados
            for (int i = 0; i < itemsToBeRemoved.size(); i++) {
                pmMap.remove(itemsToBeRemoved.get(i));
            }
        }
    }

    /**
     * Sincroniza o novo placeManager com a informação de Places atual, através da inserção de logs
     */
    private void syncNewNode(int syncPort) {
        if (placesList != null && placesList.size() > 0) {
            for (Place p : placesList) {
                OperationLog log = new OperationLog(syncPort, p);
                logs.add(log);
            }
        }
    }

    /**
     * cria e envia uma mensagem de heartbeat
     */
    private void sendHearbeat() {
        String msg = "heartbeat," + port;
        sendMessage(msg);
    }

    /**
     * Cria e inicializa uma thread responsavel por processar os logs registados que estão pendentes
     */
    private void updateFollowers() {

        Thread updateThread = new Thread() {
            @Override
            public void run() {
                while (execute) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {}

                    if (logs.size() > 0) {
                        for (int i = 0; i < logs.size(); i++) {
                            //se o log ainda está pendente
                            if (logs.get(i).getState() == OperationLogState.PENDING) {
                                int logPort = logs.get(i).getPort();

                                try {
                                    //se o log diz respeito a outro placeManager
                                    if (logPort != port) {
                                        PlacesListInterface pm = (PlacesListInterface) Naming.lookup("rmi://localhost:" + logPort + "/placelist");
                                        if (pm != null) {
                                            OperationLog newLog = new OperationLog(logs.get(i)); //cria uma copia do log
                                            pm.addOperation(newLog); //adiciona por RMI o log ao placeManager correspondente, ainda com o estado pendente
                                            logs.get(i).setState(OperationLogState.PROCESSED);//atualiza o estado do log no placeManager atual
                                        }
                                    } else {
                                        // tenho logs para adicionar places, vou adicionar
                                        OperationLog log = logs.get(i);
                                        Place place = log.getEntry();
                                        addPlace(place);
                                        logs.get(i).setState(OperationLogState.PROCESSED);
                                    }
                                } catch (NotBoundException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
            }
        };

        updateThread.start();
    }


    /**
     * adiciona um place à lista de Places
     */
    @Override
    public boolean addPlace(Place p) throws RemoteException {
        if (!this.placesList.contains(p)) {
            this.placesList.add(p);

            if (port == leaderId) {
                // se é o lider tem de atualizar os restantes place managers
                if (this.pmMap.size() > 1) {
                    for (int pmPort : pmMap.keySet()) {
                        if (pmPort != port) {
                            OperationLog log = new OperationLog(pmPort, p);
                            this.logs.add(log);
                        }
                    }
                }
            }
            return true;
        }

        return false;
    }
    @Override
    public boolean removePlace(Place p) throws RemoteException {
        if (this.placesList.contains(p)) {
            this.placesList.remove(p);
            return true;
        }
        return false;
    }
    /**
     * adiciona um log à lista de logs
     */
    @Override
    public void addOperation(OperationLog log) throws RemoteException {
        if (log != null) {
            logs.add(log);
        }
    }

    /**
     * devolve a lista de todos os places
     */
    @Override
    public ArrayList allPlaces() throws RemoteException {
        return this.placesList;
    }

    /**
     * retorna o place referente ao código postal recebido
     */
    @Override
    public Place getPlace(String cp) throws RemoteException {
        for (int i = 0; i < this.placesList.size(); i++) {
            if (this.placesList.get(i).getPostalCode().equals(cp)) {
                return this.placesList.get(i);
            }
        }

        return null;
    }


}
