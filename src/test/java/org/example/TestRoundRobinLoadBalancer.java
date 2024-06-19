package org.example;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.example.LoadBalancer;
import org.example.RoundRobinLoadBalancer;
import org.example.Server;
import org.example.ServerFactory;
import org.junit.jupiter.api.Test;

class TestRoundRobinLoadBalancer {

	@Test
	void testServerRequest() {
		List<Server> servers = ServerFactory.createServers(7);
		LoadBalancer roundRobinLB = new RoundRobinLoadBalancer(servers);
		for (int i = 0; i < servers.size(); i++) {
			String requestId = String.valueOf(i);
			Request request = new Request(requestId, "GET");
			String serverName = roundRobinLB.serveRequest(request);
			assertEquals(servers.get(i).getName(), serverName);
		}
	}

}
