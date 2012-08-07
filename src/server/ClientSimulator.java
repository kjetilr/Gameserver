package server;

import testrig.SetPlayerNumberMessage;

public class ClientSimulator {
    private String id;
    private NetworkWorker networkWorker;

    public ClientSimulator(String id, NetworkWorker networkWorker) {
        this.id = id;
        this.networkWorker = networkWorker;
    }

    public String getId() {
        return id;
    }

    public void setNumClients(int numClients) {
        networkWorker.send(new SetPlayerNumberMessage(numClients));
        networkWorker.handleOutgoingMessages();
    }
}
