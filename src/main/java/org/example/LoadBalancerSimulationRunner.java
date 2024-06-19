package org.example;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.InputMismatchException;
import java.util.List;

public class LoadBalancerSimulationRunner {

	/**
	 * Simulates the RoundRobinLoadBalancer.
	 * 1. Generate a list of servers, and pass it to the load balancer.
	 * 2. Creates a CachedThreadPool that creates new threads as needed, but will
	 * reuse previously constructed threads when they are available.
	 * 3. Create up to 100 RequestTask objects and submit them to the thread pool.
	 * 4. Shut down the thread pool but wait for each thread to complete.
	 */
	public void runRoundRobinLB() {
		List<Server> servers = ServerFactory.createServers(7);
		LoadBalancer roundRobinLB = new RoundRobinLoadBalancer(servers);

		Random random = new Random();
		int numRequests = random.nextInt(100) + 1;
		System.out.println("Getting ready to issue " + numRequests + " number of requests...");

		ExecutorService executorService = Executors.newCachedThreadPool();

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numRequests; i++) {
			executorService.execute(new RequestTask(roundRobinLB, i + 1));
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("Simulation ended in " + elapsedTime + " milliseconds");
	}

	/**
	 * Simulates the LeastConnectedLoadBalancer.
	 * 1. Generate a list of servers, and pass it to the load balancer.
	 * 2. Creates a CachedThreadPool that creates new threads as needed, but will
	 * reuse previously constructed threads when they are available.
	 * 3. Create up to 100 RequestTask objects and submit them to the thread pool.
	 * 4. Shut down the thread pool but wait for each thread to complete.
	 */
	public void runLeastConnectedLB() {
		List<Server> servers = ServerFactory.createServers(7);
		LoadBalancer leastConnectedLB = new LeastConnectedLoadBalancer(servers);

		Random random = new Random();
		int numRequests = 4000;//random.nextInt(100) + 1;
		System.out.println("Getting ready to issue " + numRequests + " number of requests...");

		ExecutorService executorService = Executors.newCachedThreadPool();

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numRequests; i++) {
			executorService.execute(new RequestTask(leastConnectedLB, i + 1));
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("Simulation ended in " + elapsedTime + " milliseconds");
	}

	/**
	 * Simulates a LoadBalancer.
	 * 1. Prompts user to choose either Round Robin Load Balancer
	 * or Least Connected Load Balancer.
	 * 2. Runs the chosen load balancer.
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		LoadBalancerSimulationRunner sim = new LoadBalancerSimulationRunner();
		String invalidMessage = "Invalid choice. Please enter 1 or 2";
		while (true) {
			System.out.print(
					"\n1. Round Robin Load Balancer\n" + 
			"2. Least Connected Load Balancer\n" +
			"\nPlease enter 1 or 2: ");
			try {
				int choice = scanner.nextInt();
				if (choice == 1) {
					sim.runRoundRobinLB();
					break;
				} else if (choice == 2) {
					sim.runLeastConnectedLB();
					break;
				}
			} catch (InputMismatchException e) {
				e.printStackTrace();
				scanner.next();
			}
			System.out.println(invalidMessage);
		}

	}

}
