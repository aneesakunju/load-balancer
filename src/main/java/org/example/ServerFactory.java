package org.example;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerFactory {

    private static final int ALPHABET_LENGTH = 26;

    /**
     * Creates a list of servers, starting from 'a' to 'z',
     * then 'a1' to 'z1', and so on.
     * 
     * @param numServers the number of servers to create
     * @return a List of servers
     */
    public static List<Server> createServers(int numServers) {
        List<Server> servers = new CopyOnWriteArrayList<>();

        char serverNamePrefix = 'a';
        for (int i = 0; i < numServers; i++) {
            String serverName = generateServerName(serverNamePrefix, i);
            servers.add(new Server(serverName));
            serverNamePrefix = incrementServerNamePrefix(serverNamePrefix);
        }

        return servers;
    }

    private static String generateServerName(char serverNamePrefix, int index) {
        StringBuilder serverName = new StringBuilder();
        serverName.append(serverNamePrefix);
        if (index >= ALPHABET_LENGTH) {
            serverName.append(index/ALPHABET_LENGTH);
        }
        return serverName.toString();
    }

    private static char incrementServerNamePrefix(char serverNamePrefix) {
        return serverNamePrefix == 'z' ? 'a' : (char) (serverNamePrefix + 1);
    }
}
