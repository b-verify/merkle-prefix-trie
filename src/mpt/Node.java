package mpt;

public interface Node {
		
	byte[] getHash();

	byte[] getKey();
	
	byte[] getKeyHash();
	
	byte[] getValue();
		
	boolean isLeaf();
	
	boolean isEmpty();
				
	Node getLeftChild();
		
	Node getRightChild();
		
}
