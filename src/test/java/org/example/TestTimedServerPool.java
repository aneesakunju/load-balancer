package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestTimedServerPool {

	@Test
	void testGetSingleServer() {
		TimedServerPool pool = new TimedServerPool(5);
		pool.put("a", 5_000);
		String serverName = pool.get("a");
		assertEquals("a", serverName);
	}
	
	@Test
	void testGetSingleServerAfterDelay() {
		TimedServerPool pool = new TimedServerPool(5);
		pool.put("a", 1_000);
		try {
			Thread.sleep(2_000);
			String serverName = pool.get("a");
			assertNull(serverName);
		} catch (InterruptedException e) {
			fail("invalid server name");
		}
	}
	
	@Test
	void testGetServerHasntTimedOut() {
		TimedServerPool pool = new TimedServerPool(5);
		pool.put("a", 5_000);
		pool.put("b", 3_000);
		try {
			Thread.sleep(2_000);
			String serverName = pool.get("b");
			assertEquals("b", serverName);
		} catch (InterruptedException e) {
			fail("Invalid server name");
		}
	}
	
	@Test
	void testSizeOneServer() {
		TimedServerPool pool = new TimedServerPool(5);
		pool.put("a", 5_000);
		assertEquals(1, pool.size());
	}
	
	@Test
	void testSizeOneServerReleased() {
		TimedServerPool pool = new TimedServerPool(5);
		pool.put("a", 5_000);
		pool.put("b", 3_000);
		try {
			Thread.sleep(4_000);
			assertEquals(1, pool.size());
		} catch (InterruptedException e) {
			fail("Invalid server name");
		}
	}

	@Test
	void testIsEmpty() {
		TimedServerPool pool = new TimedServerPool(5);
		assertTrue(pool.isEmpty());
	}

}
