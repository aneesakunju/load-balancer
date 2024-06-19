package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private List<Server> servers;
    private AtomicInteger currentIndex;
    private Map<String, Integer> serverToRequestCount;

	/**
	 * Constructor
	 *
	 * @param servers the list of servers to be used
	 */
    public RoundRobinLoadBalancer(List<Server> servers) {
    	this.currentIndex = new AtomicInteger(0);
        this.servers = servers;
        this.serverToRequestCount = new ConcurrentHashMap<>();
        setup(servers);
    }

	/**
	 * Assigns each server with a request count of 0.
	 *
	 * @param servers the list of servers
	 */
    private void setup(List<Server> servers) {
    	for (Server server : servers) {
    		serverToRequestCount.put(server.getName(), 0);
    	}
    }

	/**
	 * Determines the next server in the round robin, and issues the request to it.
	 *
	 * @param request the request that the LB will issue to the next server.
	 */
    public String serveRequest(Request request) {
        Server server = getNextServer();
        server.handleIncomingRequest(request);
        incrementRequestCount(server.getName());
        return server.getName();
    }

	/**
	 * Finds the next server in the round robin.
	 *
	 * @return the next server.
	 */
    private Server getNextServer() {
    	int index = currentIndex.getAndUpdate(i -> (i + 1) % servers.size());
    	return servers.get(index);
    }

	/**
	 * Gets the status of the servers.
	 *
	 * @return the status of the servers.
	 */
    @Override
    public synchronized String getStatus() {
    	StringBuilder status = new StringBuilder();
        // private Map<String, Integer> serverToRequestCount;
    	for (Map.Entry<String, Integer> entry : serverToRequestCount.entrySet()) {
    		status.append("Server name: " + entry.getKey());
    		status.append(", active connections: " + entry.getValue());
    		status.append("\n");
    	}
        return status.toString();
    }

	/**
	 * Increments the no of requests that a server has by 1.
	 *
	 * @param serverName the server name.
	 */
	@Override
	public synchronized void incrementRequestCount(String serverName) {
		if (serverToRequestCount.containsKey(serverName)) {
			serverToRequestCount.put(serverName, serverToRequestCount.get(serverName) + 1);
		}
	}

	/**
	 * Decrements the no of requests that a server has by 1.
	 *
	 * @param serverName the server name.
	 */
	@Override
	public synchronized void decrementRequestCount(String serverName) {
		if (serverToRequestCount.containsKey(serverName)) {
			serverToRequestCount.put(serverName, serverToRequestCount.get(serverName) - 1);
		}
	}

}
