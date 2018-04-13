package mpt;

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
	
	public static final String EMPTY_MSG = "<EmptyLeafNode Hash: " + Utils.byteArrayAsHexString(EMPTY_HASH) + ">";

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
		throw new RuntimeException("tried to serialize an empty node - fatal error");
	}
	
}
