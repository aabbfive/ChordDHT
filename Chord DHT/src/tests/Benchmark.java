package tests;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dht.DHT;
import dht.Node;
import dht.NodeImpl;

public class Benchmark {
	@SuppressWarnings("unchecked")
	public static void benchConcurrency(int n) {
		try {
			long start, end;
			DHT<String> dht = new NodeImpl<>("table");
			
			System.out.println("Creating " + n + " nodes.");
			start = System.currentTimeMillis();
			List<DHT<String>> nodes = createNodes(n, (Node<String>) dht);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
			System.out.println("Putting " + n + " values.");
			start = System.currentTimeMillis();
			List<String> keys = benchPut(nodes);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
			System.out.println("Getting " + keys.size() + " random values from DHT");
			start = System.currentTimeMillis();
			benchGet(nodes, keys);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static List<DHT<String>> createNodes(int n, final Node<String> dht) throws RemoteException {
		final List<DHT<String>> nodes = new ArrayList<>();
		for(int i=0; i<n; i++) {
			nodes.add(new NodeImpl<>("node"+i, dht));
		}
		return nodes;
	}
	
	private static List<String> benchPut(List<DHT<String>> dhts) throws InterruptedException {
		Random rand = new Random();
		final List<String> keys = new ArrayList<>();
		final Lock write = new ReentrantLock();
		
		final AtomicInteger finnishedWork = new AtomicInteger();
		int workStarted = 0;
		
		for(final DHT<String> dht : dhts) {
			final String key = "key" + rand.nextInt();
			new Thread(){
				public void run() {
					dht.put(key, "v");
					write.lock();
					keys.add(key);
					finnishedWork.incrementAndGet();
					write.unlock();
				}
			}.start();
			workStarted++;
		}
		while(finnishedWork.get() != workStarted);
//			System.out.println("workStarted=" + workStarted + ", finnishedWork=" + finnishedWork);
//		System.out.println("Done! workStarted=" + workStarted + ", finnishedWork=" + finnishedWork);
		return keys;
	}
	
	private static void benchGet(List<DHT<String>> dhts, List<String> keys) {
		Random rand = new Random();
		
		final AtomicInteger finnishedWork = new AtomicInteger();
		int workStarted = 0;
		
		for(final DHT<String> dht : dhts) {
			final String key = keys.get(rand.nextInt(keys.size()));
			new Thread() {
				public void run() {					
					dht.get(key);
					finnishedWork.incrementAndGet();
				}
			}.start();
			workStarted++;
		}
		
		while(finnishedWork.get() != workStarted);
	}

	@SuppressWarnings("unchecked")
	public static void bench(int nodes, int n) {
		try {
			long start, end;
			String[] keys = new String[n];
			for(int i=0; i<n; i++)
				keys[i] = "key" + i;
			
			System.out.println("Starting test with " + nodes + " nodes and " + n + " operations.");
			
			start = System.currentTimeMillis();
			DHT<String> dht = new NodeImpl<>("dht");
			for(int i=0; i<nodes; i++)
				new NodeImpl<>("dht"+i, (Node<String>)dht);
			end = System.currentTimeMillis();
			System.out.println("Create time: " + (int)(end-start));
			
			
			start = System.currentTimeMillis();
			for (String key : keys)
				dht.put(key, "value");
			end = System.currentTimeMillis();
			System.out.println("Put time: " + (int)(end-start));
			
			
			start = System.currentTimeMillis();
			for (String key : keys)
				dht.get(key);
			end = System.currentTimeMillis();
			System.out.println("Get time: " + (int)(end-start));
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}