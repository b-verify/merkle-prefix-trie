package mpt.core;

import serialization.generated.MptSerialization;

/**
 * Nodes are the building blocks of the MPT data structure. 
 * A node is MUTABLE - the children of interior nodes can change
 * and the value stored at a leaf node can change. Nodes track 
 * these changes and re-calculate hashes accordingly.
 * 
 * @author henryaspegren
 *
 */
public interface Node {	

	/**
	 * Get the value stored at this node, if it exists. This is only 
	 * applicable for a non-empty leaf. 
	 * @return
	 */
	byte[] getValue();
	
	/**
	 * Set the value stored at this node, if it exists. This is only 
	 * applicable for a non-empty leaf.
	 * @param value
	 */
	void setValue(byte[] value);
	
	/**
	 * Get the hash of this node. 
	 * @return
	 */
	byte[] getHash();
	
	/**
	 * Count the number of hashes required to calculate 
	 * getHash()
	 * @return
	 */
	int countHashesRequiredForGetHash();

	/**
	 * Get the key stored at this node, if it exists. This is only 
	 * applicable for a non-empty leaf.
	 * @return
	 */
	byte[] getKey();
		
	/**
	 * Returns true if this node is a (possibly empty) leaf
	 * @return
	 */
	boolean isLeaf();
	
	/**
	 * Returns true if this node is an empty leaf 
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * Returns true if this node is a stub 
	 * @return
	 */
	boolean isStub();
				
	/**
	 * Returns the left child of this node, if it exists. 
	 * Only applicable if this is an interior node.
	 * @return
	 */
	Node getLeftChild();
		
	/**
	 * Returns the right child of this node, if it exists.
	 * Only applicable if this is an interior node. 
	 * @return
	 */
	Node getRightChild();
	
	/**
	 * Set the left child of this node, if possible. 
	 * Only applicable if this is an interior node.
	 * @param leftChild
	 */
	void setLeftChild(Node leftChild);
	
	/**
	 * Set the right child of this node, if possible. 
	 * Only applicable if this is an interior node.
	 * @param rightChild
	 */
	void setRightChild(Node rightChild);
	
	/**
	 * Returns true if this node has been changed
	 * @return
	 */
	boolean changed();
	
	/**
	 * Marks the entire (sub)tree rooted at this node 
	 * as changed
	 */
	void markChangedAll();
	
	/**
	 * Marks the entire (sub)tree rooted at this node
	 * as unchanged
	 */
	void markUnchangedAll();
	
	/**
	 * Return a (recursive) serialization of this node
	 * @return
	 */
	MptSerialization.Node serialize();
	
	/**
	 * Return the number of nodes of any kind in the subtree rooted 
	 * at this node (includes this node).
	 * @return
	 */
	int nodesInSubtree();
	
	/**
	 * Return the number of interior nodes in the 
	 * subtree rooted at this node (includes this node).
	 * @return
	 */
	int interiorNodesInSubtree();
	
	/**
	 * Return the number of empty leaf nodes
	 * in the subtree rooted at this node (includes this node).
	 * @return
	 */
	int emptyLeafNodesInSubtree();
	
	/**
	 * Return the number of non-empty leaf nodes 
	 * in the subtree rooted at this node (includes this node)
	 * @return
	 */
	int nonEmptyLeafNodesInSubtree();

}
