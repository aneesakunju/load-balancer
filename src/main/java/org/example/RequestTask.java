package org.example;

import java.util.Random;

public class RequestTask implements Runnable {
	
	private final LoadBalancer loadBalancer;
	private final int requestId;
	private Random random;

	/**
	 * Constructor
	 *
	 * This class is a Runnable. The cached thread pool will execute each RequestTask
	 * object.
	 *
	 * @param loadBalancer the loadBalancer object that gets the request.
	 * @param requestId the requestId.
	 */
	public RequestTask(LoadBalancer loadBalancer, int requestId) {
		this.loadBalancer = loadBalancer;
		this.requestId = requestId;
		random = new Random();
	}

	/**
	 * A Request object is created, which is passed to the loadBalancer.
	 * The loadBalancer serves the Request to the next Server, waits for
	 * the request to complete (ie timeForRequestRun which is random),
	 * and then decrements the request count of that server.
	 *
	 */
	@Override
	public void run() {
		String clientId = String.valueOf(requestId);
		Request request = new Request(clientId, "GET");
		// mock stagger to space out the requests
		long delayBeforeStartRequest = random.nextInt(3_000);
		try {
			Thread.sleep(delayBeforeStartRequest);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String serverName = loadBalancer.serveRequest(request);
		System.out.println("--------------------------------------");
		if (serverName == null) {
			System.out.println("Unable to service request: " + request);
		} else {
			// each request has a mocked random time to complete
			long timeForRequestRun = random.nextInt(5_000);
			try {
				Thread.sleep(timeForRequestRun);
				loadBalancer.decrementRequestCount(serverName);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Completed Request#" + requestId);
		}
		System.out.println("\n" + loadBalancer.getStatus() + "\n");
		System.out.println("--------------------------------------");
	}

}
