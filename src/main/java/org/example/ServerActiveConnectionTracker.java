package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerActiveConnectionTracker {
	// Map to track server names and their corresponding request counts in
	// the minHeap, in O(1) time.
	private Map<String, ServerCountPair> serverToServerCountPair;
	// Priority queue of ServerCountPair objects, that sorts servers according to their
	// lowest request count
	private PriorityBlockingQueue<ServerCountPair> minHeap;

	/**
	 * Constructor
	 */
	public ServerActiveConnectionTracker() {
		serverToServerCountPair = new ConcurrentHashMap<>();
		minHeap = new PriorityBlockingQueue<>();
	}

	/**
	 * Adds a server to the minHeap, with a request count of 0.
	 *
	 * @param serverName the server name
	 */
	public void addServer(String serverName) {
		updateServerCount(serverName, 0);
	}

	/**
	 * Removes a server from the minHeap.
	 *
	 * @param serverName the server name
	 */
	public synchronized void removeServer(String serverName) {
		if (!serverToServerCountPair.containsKey(serverName)) {
			return;
		}
		ServerCountPair pair = serverToServerCountPair.get(serverName);
		minHeap.remove(pair);
		serverToServerCountPair.remove(serverName);
	}

	/**
	 * Updates the requestCount of a server in minHeap and serverToServerCountPair map.
	 * countDelta=0 during init server,
	 * countDelta=1 when adding request to the server,
	 * countDelta=-1 for removing request from the server
	 * Note that each server that's added/removed from minHeap, is also
	 * added/removed from serverToServerCountPair. serverToServerCountPair keeps track
	 * of what servers exist on minHeap. Map access is O(1) while minHeap access is O(logn)
	 *
	 * @param serverName the server name
	 * @param countDelta the request count delta to add to the server
	 */
	public synchronized void updateServerCount(String serverName, int countDelta) {
		ServerCountPair pair = serverToServerCountPair.get(serverName);
		// If map contains server, it exists on minHeap too.
		// So remove object from minHeap, update its count, add it back to minHeap, to trigger sort.
		if (pair != null) {
			minHeap.remove(pair);
			pair.count += countDelta;
			minHeap.offer(pair);
		} else {
			pair = new ServerCountPair(countDelta, serverName);
			serverToServerCountPair.put(serverName, pair);
			minHeap.offer(pair);
		}
	}

	/**
	 * Retrieves and removes the least connected server, ie the server with the lowest request count,
	 * which will be at head of minHeap
	 *
	 * @return the least connected server.
	 */
	public synchronized String getLeastUsedServer() {
		// If min heap is empty, return null
		if (minHeap.isEmpty()) {
			return null;
		}

		ServerCountPair pair = minHeap.poll();
		String serverName = pair.serverName;
		return serverName;
	}

	/**
	 * Gets minHeap size
	 *
	 * @return the minHeap size.
	 */
	public int size() {
		assert(minHeap.size() == serverToServerCountPair.size());
		return minHeap.size();
	}

	/**
	 * Checks if minHeap is empty
	 *
	 * @return true if minHeap size is 0.
	 */
	public boolean isEmpty() {
		assert(minHeap.isEmpty() == serverToServerCountPair.isEmpty());
		return minHeap.size() == 0;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("ServerActiveConnectionTracker [");
		for (Map.Entry<String, ServerCountPair> entrySet : serverToServerCountPair.entrySet()) {
			result.append("\nserver=");
			result.append(entrySet.getKey());
			result.append(", active connections=");
			result.append(entrySet.getValue().count);
			result.append("; ");
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * Inner class to represent (serverName, count) pair,
	 * where count represents the number of active requests of the server.
	 *
	 */
	private class ServerCountPair implements Comparable<ServerCountPair> {
		int count;
		String serverName;

		public ServerCountPair(int count, String serverName) {
			this.count = count;
			this.serverName = serverName;
		}

		/**
		 * Sorts servers according their request counts, in ascending order.
		 * This allows for the head of the minHeap to have the least connected server,
		 * ie the server with the lowest no of request count. Allows retrieval of the
		 * least connected server in O(1) time.
		 * If there are >1 servers with the lowest request count, break ties with
		 * server name lexicographic sort.
		 *
		 * @return a positive, 0, or negative number.
		 */
		@Override
		public int compareTo(ServerCountPair other) {
			// Compare by count, breaking ties by comparing server names lexicographically
			if (this.count != other.count) {
				return Integer.compare(this.count, other.count);
			}
			return this.serverName.compareTo(other.serverName);
		}
	}
}
