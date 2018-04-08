package mpt;

import crpyto.CryptographicDigest;

public class EmptyLeafNode implements Node {
	

	// hash is all zeros
	public static final byte[] EMPTY_HASH = new byte[CryptographicDigest.getSizeBytes()];
	
	public static final String EMPTY_MSG = "<EmptyLeafNode H: "+EMPTY_HASH+">";

	@Override
	public byte[] getValue() {
		return EmptyLeafNode.EMPTY_HASH;
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
		return EmptyLeafNode.EMPTY_HASH;
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
}
