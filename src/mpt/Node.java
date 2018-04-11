package mpt;

public interface Node {	

	byte[] getHash();

	byte[] getKey();
		
	byte[] getKeyHash();
	
	byte[] getValue();
	
	boolean isLeaf();
	
	boolean isEmpty();
	
	boolean isStub();
				
	Node getLeftChild();
		
	Node getRightChild();
	
	serialization.MptSerialization.Node serialize();
		
}
