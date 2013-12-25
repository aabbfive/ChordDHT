package sub1;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * The implementation of a node/peer in the distributed hash table.
 * Interfaces for usage and node-to-node communication are implemented.
 * @author Andreas
 * @param <E>
 *
 */
public class NodeImpl<E> extends UnicastRemoteObject implements Node<E>, DHT<E> {

	private static final long serialVersionUID = 7837010474371220959L;
	
	private String name;
	private String key;
	private Node<E> successor;
	private Node<E> predecessor;
	private HashMap<String, E> storage = new HashMap<>();
	
	private static final int N = 10;
	public static final int DEFAULT_PORT = 1099;
	
	/**
	 * Constructor to initiate a single node.
	 * @param name
	 * @throws RemoteException
	 */
	public NodeImpl(String name) throws RemoteException {
		this.name = name;
		key = Key.generate(name, N);
		successor = this;
		predecessor = this;
		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(DEFAULT_PORT);
//			System.out.println("Create Registry");
		} catch (Exception e) {
			registry = LocateRegistry.getRegistry(DEFAULT_PORT);
//			System.out.println("Get registry.");
		}
		registry.rebind(name, this);
	}
	
	/**
	 * Constructor used to join another node (can be remote).
	 * @param name
	 * @param other
	 * @throws RemoteException
	 */
	public NodeImpl(String name, Node<E> other) throws RemoteException {
		this(name);
		join(other);
	}
	
	/**
	 * Constructor used to join a Node on a remote network.
	 * @param name
	 * @param host
	 * @param port
	 * @param ohterName
	 * @throws RemoteException
	 * @throws NotBoundException 
	 */
	@SuppressWarnings("unchecked")
	public NodeImpl(String name, String host, int port, String ohterName) throws RemoteException, NotBoundException {
		this(name);
		Registry registry = LocateRegistry.getRegistry(host, port);
		Node<E> other = (Node<E>) registry.lookup(ohterName);
		join(other);
	}
	
	@Override
	public void join(Node<E> other) {
		try {
		boolean joined = false;
			while(!joined) {
				Node<E> pred = other.getPredecessor();
				String otherKey = other.getKey();
				String predKey = pred.getKey();
				
				if(Key.between(key, predKey, otherKey)) {
					pred.setSuccessor(this);
					other.setPredecessor(this);
					setSuccessor(other);
					setPredecessor(pred);
					joined = true;
	//				System.out.println(name + ": Joined in between " + pred + " and " + other);
				} else
					other = other.getSuccessor();
			}
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void leave() {
		try {
			successor.setPredecessor(predecessor);
			predecessor.setSuccessor(successor);
			successor = this;
			predecessor = this;
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	@Override
	public Node<E> getSuccessor() throws RemoteException {
		return successor;
	}

	@Override
	public Node<E> getPredecessor() throws RemoteException {
		return predecessor;
	}

	@Override
	public void setSuccessor(Node<E> succ) throws RemoteException {
		successor = succ;
	}

	@Override
	public void setPredecessor(Node<E> pred) throws RemoteException {
		predecessor = pred;
	}

	@Override
	public void probe(String key, int count) throws RemoteException {
		if(this.key.equals(key) && count > 0) {
			System.out.println("Probe returned after " + count + " hops.");
		} else {
			System.out.println(name + ": Forwarding probe to " + successor);
			successor.probe(key, count+1);
		}
	}

	@Override
	public Node<E> lookup(String key) throws RemoteException {
		String predKey = predecessor.getKey();
		if(Key.between(key, predKey, this.key)) {
			return this;
		} else {
			return successor.lookup(key);
		}
	}
	
	@Override
	public E getStored(String key) throws RemoteException {
		return storage.get(key);
	}
	
	@Override
	public void addStored(String key, E value) throws RemoteException {
		storage.put(key, value);
	}
	
	@Override
	public void removeStored(String key) throws RemoteException {
		storage.remove(key);
	}

	@Override
	public E get(String key) {
		try {
			String k = Key.generate(key, N);
			Node<E> node = lookup(k);
//			System.out.println(name + ": " + node + " have that.");
			return node.getStored(k);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void put(String key, E object) {
		try {
			String k = Key.generate(key, N);
			Node<E> node = lookup(k);
//			System.out.println(name + ": " + node + " wants that.");
			node.addStored(k, object);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void remove(String key) {
		try {
			String k = Key.generate(key, N);
			Node<E> node = lookup(k);
//			System.out.println(name + ": " + node + " had that.");
			node.removeStored(k);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> listAll() {
		Node<E> currentNode = this;
		ArrayList<String> all = new ArrayList<>();
		try {
			do {
				for(E element : currentNode.getValues())
					all.add(element.toString());
				currentNode = currentNode.getSuccessor();
			} while(currentNode != this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return all;
	}

	@Override
	public Collection<E> getValues() throws RemoteException {
		return storage.values();
	}

}
