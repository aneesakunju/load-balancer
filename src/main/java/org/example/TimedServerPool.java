package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimedServerPool {

	private final Map<String, TimedValue> serverToTimedValue;
	// Each thread in the thread pool represents a server and will delay <server's expiryTime> and then
	// run removeExpiredEntries() to remove the server from the list of available servers.
	private final ScheduledExecutorService executorService;
	private Lock lock = new ReentrantLock();
	/**
	 * Constructs a TimedServerPool object that consists of a Map and a ScheduledExecutorService.
	 *
	 * The ScheduledExecutorService is a thread pool that can schedule commands
	 * to run after a given delay. The thread pool will have a thread count equal to the no of servers.
	 * Here, the thread pool is used to simulate the server pool, where each thread represents a server
	 * being alive. The thread's delay represents the expiryTime of the server. Upon the delay elapsing,
	 * the thread runs the removeExpiredEntries() method which will remove the server from the list of
	 * available servers.
	 *
	 * @param numThreads the number of threads, which is the no of servers.
	 */
	public TimedServerPool(int numThreads) {
		serverToTimedValue = new ConcurrentHashMap<>();
		this.executorService = Executors.newScheduledThreadPool(numThreads);
	}

	/**
	 * Add a server to the pool and its expiry time.
	 * Also calls startCleanupTask() to schedule the server to be removed from the pool
	 * at the expiry time.
	 * 
	 * @param key              the server name
	 * @param expiryTimeMillis how long the server will be kept in the pool
	 */
	public void put(String key, long expiryTimeMillis) {
		lock.lock();
		try {
			serverToTimedValue.put(key, new TimedValue(key, expiryTimeMillis));
			// Remove server from pool after it times out, ie in expiryTimeMillis.
			startCleanupTask(expiryTimeMillis);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the TimedValue based on the key name
	 * 
	 * @param key the key to use
	 * @return the TimedValue object
	 */
	public String get(String key) {
		lock.lock();
		try {
			TimedValue timedValue = serverToTimedValue.get(key);
			return timedValue != null ? timedValue.getKey() : null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the time value based on the key name
	 *
	 * @param expiryTimeMillis how much time the server is acquired for
	 */
	private void startCleanupTask(long expiryTimeMillis) {
		long initialDelayInMilliseconds = expiryTimeMillis;
		executorService.schedule(this::removeExpiredEntries, initialDelayInMilliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * Removes an expired server from serverToTimedValue map.
	 * This method is invoked by a thread in the scheduled thread pool and signifies
	 * that a server's expiryTime has elapsed and thus needs to be removed.
	 *
	 */
	private void removeExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		String serverToRemove = null;
		TimedValue timedValue = null;

		lock.lock();
		try {
			for (Map.Entry<String, TimedValue> entry : serverToTimedValue.entrySet()) {
				if (entry.getValue().isExpired(currentTime)) {
					serverToRemove = entry.getKey();
					timedValue = entry.getValue();
					break;
				}
			}
			if (serverToRemove != null) {
				serverToTimedValue.remove(serverToRemove);
				System.out.println("==> Released Server " + serverToRemove + ", total time server [expiry time = "
						+ +timedValue.expiryTimeMillis + " ms] was acquired until time of release: "
						+ timedValue.getElapsedTime() + " ms");
			}
			if (serverToTimedValue.isEmpty()) {
				shutdown();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns number of servers remaining (still alive).
	 *
	 * @return the number of servers remaining
	 */
	public int size() {
		lock.lock();
		try {
			return serverToTimedValue.size();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks if there are any servers remaining (still alive).
	 *
	 * @return true if there are servers remaining
	 */
	public boolean isEmpty() {
		lock.lock();
		try {
			return serverToTimedValue.size() == 0;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Shuts down the scheduled thread pool.
	 */
	public void shutdown() {
		executorService.shutdown();
	}

	@Override
	public String toString() {
		lock.lock();
		try {
			StringBuilder result = new StringBuilder();
			result.append("TimedServerPool [");
			for (Map.Entry<String, TimedValue> entrySet : serverToTimedValue.entrySet()) {
				result.append("\nserver=");
				result.append(entrySet.getKey());
				result.append(", milliseconds remaining=");
				result.append(entrySet.getValue().getMillisecondsRemaining());
				result.append("; ");
			}
			result.append("]");
			return result.toString();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Inner class to represent (key, startTimestamp, expiryTimeMillis),
	 * where key is the server name
	 * where startTimestamp is the server's start timestamp,
	 * where expiryTimeMillis is the server's expiry time.
	 *
	 */
	private static class TimedValue {
		private String key;
		private long startTimestamp;
		private long expiryTimeMillis;

		TimedValue(String key, long expiryTimeMillis) {
			this.key = key;
			this.expiryTimeMillis = expiryTimeMillis;
			// timestamp at the time the server starts
			this.startTimestamp = System.currentTimeMillis();
		}

		/**
		 * Checks if server is dead or alive.
		 *
		 * @return true if server is dead/expired.
		 */
		boolean isExpired(long currentTimeMillis) {
			return currentTimeMillis - startTimestamp >= expiryTimeMillis;
		}

		/**
		 * Calculates elapsed time since server started
		 *
		 * @return elapsed time since server started.
		 */
		public long getElapsedTime() {
			return System.currentTimeMillis() - startTimestamp;
		}

		/**
		 * Calculates time remaining for server until it expires
		 *
		 * @return time remaining for server until it expires
		 */
		public long getMillisecondsRemaining() {
			return expiryTimeMillis - (System.currentTimeMillis() - startTimestamp);
		}

		String getKey() {
			return key;
		}
	}

}
