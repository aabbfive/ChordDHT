package sub1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Remote interface to let Nodes interact with each other.
 * @author Andreas
 *
 */
public interface Node<E> extends Remote {
	
	/**
	 * Get the key associated with the Node.
	 * @return a key...
	 * @throws RemoteException
	 */
	public String getKey() throws RemoteException;
	
	/**
	 * Get the successor of this node. If there are no other nodes, a node's successor will be the node itself.
	 * @return The successor node.
	 * @throws RemoteException
	 */
	public Node<E> getSuccessor() throws RemoteException;
	
	/**
	 * Get the predecessor of this node. If there are no other nodes, a node's predecessor will be the node itself.
	 * @return The predecessor node.
	 * @throws RemoteException
	 */
	public Node<E> getPredecessor() throws RemoteException;
	
	/**
	 * Set this node's successor to succ.
	 * @param succ
	 * @throws RemoteException
	 */
	public void setSuccessor(Node<E> succ) throws RemoteException;
	
	/**
	 * Set this node's predecessor to pred.
	 * @param pred
	 * @throws RemoteException
	 */
	public void setPredecessor(Node<E> pred) throws RemoteException;
	
	/**
	 * Send (or forward) a probe through the network. A probe will simply print its' name to the standard output and forward it to its' successor.
	 * @param key - Key of the node who originally sent the probe.
	 * @param count - To get information about the number of nodes (incremented each hop).
	 * @throws RemoteException
	 */
	public void probe(String key, int count) throws RemoteException;
	
	/**
	 * Search the network for a Node responsible for this key. Used in put/get/remove.
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	public Node<E> lookup(String key) throws RemoteException;
	
	/**
	 * A network-level get.
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	public E getStored(String key) throws RemoteException;
	
	/**
	 * A network-level put.
	 * @param key
	 * @param value
	 * @throws RemoteException
	 */
	public void addStored(String key, E value) throws RemoteException;
	
	/**
	 * A network-level remove.
	 * @param key
	 * @throws RemoteException
	 */
	public void removeStored(String key) throws RemoteException;
	
	/**
	 * @return all values contained in this node's storage.
	 * @throws RemoteException
	 */
	public Collection<E> getValues() throws RemoteException;
}
