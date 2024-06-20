package org.example;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LeastConnectedLoadBalancer implements LoadBalancer {

    private ServerActiveConnectionTracker serverTracker;
    private TimedServerPool acquiredServers;
    private Map<String, Server> serverNameToServer;
    private Lock lock = new ReentrantLock();

    /**
     * Constructor
     *
     * @param servers the list of servers to be used
     */
    public LeastConnectedLoadBalancer(List<Server> servers) {
        serverTracker = new ServerActiveConnectionTracker();
        acquiredServers = new TimedServerPool(servers.size());
        setupServerAcquiredTime(servers);
    }

    /**
     * Assigns each server with an expiryTime of 1-10 seconds.
     * Also adds the server to serverTracker which adds it to the minHeap
     *
     * @param servers the list of servers
     */
    private void setupServerAcquiredTime(List<Server> servers) {
        serverNameToServer = new ConcurrentHashMap<>();
        Random random = new Random();
        for (Server server : servers) {
            serverNameToServer.put(server.getName(), server);
            // a server can be acquired for [1-10] seconds
            int timePeriodInMilliseconds = random.nextInt(10_000) + 1_000; //random.nextInt(10_000) + 1_000;
            //System.out.println("Random: " + timePeriodInMilliseconds);
            acquiredServers.put(server.getName(), timePeriodInMilliseconds);
            // serverTracker will start tracking each server
            // ie 1. add serverCountPair object (with count=0) to the minHeap
            // 2. add serverCountPair object to a map (serverToServerCountPair)
            //    for quicker access of what's in the minHeap
            serverTracker.addServer(server.getName());
        }
    }

    /**
     * Determines the least connected server, and issues the request to it.
     *
     * @param request the request that the LB will issue to the next server.
     */
    @Override
    public String serveRequest(Request request) {
        Server server = getNextServer();
        if (server == null) {
            System.out.println();
        }
        if (server != null) {
            server.handleIncomingRequest(request);
            incrementRequestCount(server.getName());
        }
        return server != null ? server.getName() : null;
    }

    /**
     * Increments the no of requests that a server has by 1.
     *
     * @param serverName the server name.
     */
    public void incrementRequestCount(String serverName) {
        lock.lock();
        try {
            if (acquiredServers.get(serverName) != null) {
                serverTracker.updateServerCount(serverName, 1);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrements the no of requests that a server has by 1.
     *
     * @param serverName the server name.
     */
    public void decrementRequestCount(String serverName) {
        lock.lock();
        try {
            if (acquiredServers.get(serverName) != null) {
                serverTracker.updateServerCount(serverName, -1);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finds the least connected server and ensures that it is still alive.
     *
     * @return the least connected server.
     */
    private Server getNextServer() {
        Server server = null;
        lock.lock();
        try {
            // while minHeap is not empty
            while (!serverTracker.isEmpty() && server == null) {
                String leastConnectedServerName = serverTracker.getLeastUsedServer();
                // there is a chance that least connected server off the minHeap has timed out
                // and isn't in the acquired servers pool anymore
                String availableServerName = acquiredServers.get(leastConnectedServerName);
                if (availableServerName == null) {
                    // not in servers pool so remove from server tracker and go through while loop again
                    serverTracker.removeServer(leastConnectedServerName);
                } else {
                    server = serverNameToServer.get(availableServerName);
                }
            }
        } finally {
            lock.unlock();
        }
        return server;
    }

    /**
     * Gets the status of the servers.
     *
     * @return the status of the servers.
     */
    @Override
    public String getStatus() {
        //We need to lock the LeastConnectedLoadBalancer instance below so that another thread doesn't change
        // serverTracker or acquiredServers while a thread is running the code below.
        StringBuffer status = new StringBuffer();
        lock.lock();
        try {
            //StringBuilder status = new StringBuilder();
            status.append("Remaining acquired servers: ");
            status.append(serverTracker.size());
            status.append("\n");
            status.append(serverTracker);
            status.append("\n");
            status.append(acquiredServers);
        } finally {
            lock.unlock();
        }
        return status.toString();
    }

}
