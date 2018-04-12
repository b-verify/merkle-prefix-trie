package mpt;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * IMMUTABLE
 * 
 * Represents an interior node in the MPT. An interior node has 
 * two children, a left child and right child. Interior nodes do not store 
 * keys or values. The hash of the interior node is H(left.getHash()||right.getHash())
 * where left.getHash() (resp. right.getHash()) is the hash of the left (resp right) 
 * child.
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
	
	public MptSerialization.Node serialize() {
		MptSerialization.InteriorNode.Builder builder = MptSerialization.InteriorNode.newBuilder();
		// empty nodes are not serialized 
		// to save space
		if(!this.leftChild.isEmpty()) {
			serialization.MptSerialization.Node leftChildSerialized = this.leftChild.serialize();
			builder.setLeft(leftChildSerialized);
		}
		if(!this.rightChild.isEmpty()) {
			serialization.MptSerialization.Node rightChildSerialized = this.rightChild.serialize();
			builder.setRight(rightChildSerialized);
		}
		MptSerialization.Node node = MptSerialization.Node
				.newBuilder()
				.setInteriorNode(builder.build())
				.build();
		return node;
		
	}
	
	@Override
	public byte[] getValue() {
		return null;
	}

	@Override
	public byte[] getHash() {
		return this.hash.clone();
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
		return new String("<InteriorNode>");
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
			boolean leftEquals = this.leftChild.equals(in.leftChild);
			boolean rightEquals = this.rightChild.equals(in.rightChild);
			return leftEquals && rightEquals;
		}
		return false;
	}

	@Override
	public boolean isStub() {
		return false;
	}

}
