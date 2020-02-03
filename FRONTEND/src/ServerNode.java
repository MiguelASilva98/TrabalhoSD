public class ServerNode {
    private int id;
    private String rmiAddress;
    private boolean isLeader;

    public ServerNode(int id, String rmiAddress) {
        this.id = id;
        this.rmiAddress = rmiAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRmiAddress() {
        return rmiAddress;
    }

    public void setRmiAddress(String rmiAddress) {
        this.rmiAddress = rmiAddress;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }
}
