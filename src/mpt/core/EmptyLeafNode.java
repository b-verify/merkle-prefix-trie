package mpt.core;

import crpyto.CryptographicDigest;

/**
 * This class represents an empty leaf in the tree. Empty leaves 
 * do not have associated values and use the special marker
 * hash of all 0s. 
 * @author henryaspegren
 *
 */
public class EmptyLeafNode implements Node {
	
	// hash is all zeros
	public static final byte[] EMPTY_HASH = new byte[CryptographicDigest.getSizeBytes()];
	public static final String EMPTY_MSG = "<EmptyLeafNode>";
	
	// an empty leaf node can still be "changed" - if its location in the MPT changes
	// even though the empty leaf has a pre-defined hash value
	private boolean changed; 
	
	public EmptyLeafNode() {
		this.changed = true;
	}

	@Override
	public byte[] getValue() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public byte[] getHash() {
		return EmptyLeafNode.EMPTY_HASH.clone();
	}

	@Override
	public Node getLeftChild() {
		return null;
	}

	@Override
	public Node getRightChild() {
		return null;
	}

	@Override 
	public String toString() {
		return EmptyLeafNode.EMPTY_MSG;
	}

	@Override
	public byte[] getKey() {
		return null;
	}

	@Override
	public byte[] getKeyHash() {
		return null;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof EmptyLeafNode) {
			return true;
		}
		return false;
	}
	
	@Override 
	public boolean isStub() {
		return false;
	}

	@Override
	public serialization.MptSerialization.Node serialize() {
		return serialization.MptSerialization.Node.newBuilder()
				.setEmptyleaf(serialization.MptSerialization.EmptyLeaf.newBuilder())
				.build();
	}

	@Override
	public void setValue(byte[] value) {
		throw new RuntimeException("cannot set value of an empty leaf");
	}

	@Override
	public void setLeftChild(Node leftChild) {
		throw new RuntimeException("cannot set children of an empty leaf");		
	}

	@Override
	public void setRightChild(Node rightChild) {
		throw new RuntimeException("cannot set children of an empty leaf");				
	}

	@Override
	public boolean changed() {
		return this.changed;
	}

	@Override
	public void markChangedAll() {
		this.changed = true;
	}

	@Override
	public void markUnchangedAll() {
		this.changed = false;		
	}
	
}
