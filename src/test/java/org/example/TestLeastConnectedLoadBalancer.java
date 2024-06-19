package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class TestLeastConnectedLoadBalancer {

	@Test
	void testServerRequest1() {
		List<Server> servers = ServerFactory.createServers(3);
		LoadBalancer leastConnectedLB = new LeastConnectedLoadBalancer(servers);
		leastConnectedLB.incrementRequestCount("a");
		leastConnectedLB.incrementRequestCount("a");
		leastConnectedLB.incrementRequestCount("a");
		leastConnectedLB.incrementRequestCount("b");
		leastConnectedLB.incrementRequestCount("b");
		Request request = new Request("1", "GET");
		String serverName = leastConnectedLB.serveRequest(request);
		assertEquals("c", serverName);
	}
	
	@Test
	void testServerRequest2() {
		List<Server> servers = ServerFactory.createServers(4);
		LoadBalancer leastConnectedLB = new LeastConnectedLoadBalancer(servers);
		leastConnectedLB.incrementRequestCount("a");
		leastConnectedLB.incrementRequestCount("b");
		leastConnectedLB.incrementRequestCount("c");
		leastConnectedLB.incrementRequestCount("d");
		Request request = new Request("1", "GET");
		String serverName = leastConnectedLB.serveRequest(request);
		assertEquals("a", serverName);
	}
	
	@Test
	void testServerRequest3() {
		List<Server> servers = ServerFactory.createServers(4);
		LoadBalancer leastConnectedLB = new LeastConnectedLoadBalancer(servers);
		Request request = new Request("1", "GET");
		String serverName = leastConnectedLB.serveRequest(request);
		assertEquals("a", serverName);
	}

}
