package org.example;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.example.Server;
import org.example.ServerFactory;
import org.junit.jupiter.api.Test;

class TestServerFactory {

	@Test
	void testCreateServersSize() {
		List<Server> servers = ServerFactory.createServers(7);
		assertEquals(7, servers.size());
	}
	
	@Test
	void testCreateServersName() {
		String letters = "abcdefgh";
		List<Server> servers = ServerFactory.createServers(letters.length());
		for (int i = 0; i < letters.length(); i++) {
			String letter = Character.toString(letters.charAt(i));
			assertEquals(letter, servers.get(i).getName());
		}
	}

}
