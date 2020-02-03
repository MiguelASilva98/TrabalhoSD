import java.io.Serializable;

public class OperationLog implements Serializable {
    private long timeStamp;
    private int port;
    private Place entry;
    private OperationLogState state;

    public OperationLog(int port) {
        this.port = port;
        this.timeStamp = System.currentTimeMillis();
        this.state = OperationLogState.PENDING;
    }

    public OperationLog(int port, Place entry) {
        this(port);
        this.entry = entry;
    }

    public OperationLog(OperationLog log) {
        this(log.getPort(), log.getEntry());
    }

    public long getTimeStamp() {
        return timeStamp;
    }


    public int getPort() {
        return port;
    }

    public Place getEntry() {
        return entry;
    }

    public OperationLogState getState() {
        return state;
    }

    public void setState(OperationLogState state) {
        this.state = state;
    }


    public void addEntry(Place entry) {
        this.entry = entry;
    }
}
