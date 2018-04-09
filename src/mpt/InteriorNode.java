package mpt;

import java.util.Arrays;

import crpyto.CryptographicDigest;

/**
 * IMMUTABLE
 * 
 * @author henryaspegren
 *
 */
public class InteriorNode implements Node {
	
	private final byte[] hash;
	private final Node leftChild;
	private final byte[] leftChildHash;
	private final Node rightChild;
	private final byte[] rightChildHash;
	
	public InteriorNode(Node leftChild, Node rightChild) {
		this.leftChild = leftChild;
		this.leftChildHash = leftChild.getHash();
		this.rightChild = rightChild;
		this.rightChildHash = rightChild.getHash();
		
		// commitment: H(leftChildHash || rightChildHash)
		byte[] commitment = new byte[this.leftChildHash.length+this.rightChildHash.length];
		System.arraycopy(this.leftChildHash, 0, commitment, 0, this.leftChildHash.length);
		System.arraycopy(this.rightChildHash, 0, commitment, this.leftChildHash.length, this.rightChildHash.length);
		this.hash = CryptographicDigest.digest(commitment);
	}

	@Override
	public byte[] getValue() {
		return null;
	}

	@Override
	public byte[] getHash() {
		return this.hash;
	}

	@Override
	public Node getLeftChild() {
		return this.leftChild;
	}

	@Override
	public Node getRightChild() {
		return this.rightChild;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		return new String("<InteriorNode| L: "+this.leftChild+ " R: "+this.rightChildHash+">");
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
		if (arg0 instanceof InteriorNode) {
			InteriorNode in = (InteriorNode) arg0;
			return Arrays.equals(this.hash, in.hash);
		}
		return false;
	}

}
