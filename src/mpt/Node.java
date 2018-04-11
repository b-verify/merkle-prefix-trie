package mpt;

/**
 * 
 * @author henryaspegren
 *
 */
public interface Node {	

	byte[] getValue();
	
	byte[] getHash();

	byte[] getKey();
		
	byte[] getKeyHash();
		
	boolean isLeaf();
	
	boolean isEmpty();
	
	boolean isStub();
				
	Node getLeftChild();
		
	Node getRightChild();
	
	serialization.MptSerialization.Node serialize();
		
}
