package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestServerActiveConnectionTracker {

	@Test
	void testMinHeapSize3() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		tracker.addServer("a");
		tracker.addServer("b");
		tracker.addServer("c");
		assertEquals(3, tracker.size());
	}
	
	@Test
	void testMinHeapSize() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		assertEquals(0, tracker.size());
	}
	
	@Test
	void testMinHeapIsEmpty() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		assertTrue(tracker.isEmpty());
	}
	
	@Test
	void testGetLeastUsedServerOfThree() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		tracker.addServer("a");
		tracker.addServer("b");
		tracker.addServer("c");
		tracker.updateServerCount("a", 1);
		tracker.updateServerCount("b", 1);
		assertEquals("c", tracker.getLeastUsedServer());
	}
	
	@Test
	void testGetLeastUsedServer() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		tracker.addServer("a");
		tracker.addServer("b");
		tracker.addServer("c");
		tracker.updateServerCount("a", 5);
		tracker.updateServerCount("b", 2);
		tracker.updateServerCount("c", 8);
		assertEquals("b", tracker.getLeastUsedServer());
	}
	
	@Test
	void testGetLeastUsedServerLexicalBreakTies() {
		ServerActiveConnectionTracker tracker = new ServerActiveConnectionTracker();
		tracker.addServer("a");
		tracker.addServer("b");
		tracker.addServer("c");
		tracker.updateServerCount("a", 5);
		tracker.updateServerCount("b", 5);
		tracker.updateServerCount("c", 5);
		assertEquals("a", tracker.getLeastUsedServer());
	}

}
