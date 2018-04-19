package mpt;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * (MUTABLE) 
 * 
 * Represents an interior node in the MPT. An interior node has 
 * two children, a left child and right child. Interior nodes do not store 
 * keys or values. The hash of the interior node is H(left.getHash()||right.getHash())
 * where left.getHash() (resp. right.getHash()) is the hash of the left (resp right) 
 * child.
 * 
 * The children of the interiornode may be changed. Whenever the children are changed
 * the node is marked "changed" until reset() is called. Hashes are calculated
 * lazily, only when getHash() is called.
 * 
 * @author henryaspegren
 *
 */
public class InteriorNode implements Node {
	
	private byte[] hash;
	private boolean recalculateHash;
	private boolean changed;
	private Node leftChild;
	private Node rightChild;
	
	public InteriorNode(Node leftChild, Node rightChild) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.changed = true;
		this.recalculateHash = true;
	}
	
	public MptSerialization.Node serialize() {
		MptSerialization.InteriorNode.Builder builder = MptSerialization.InteriorNode.newBuilder();
		serialization.MptSerialization.Node leftChildSerialized = this.leftChild.serialize();
		serialization.MptSerialization.Node rightChildSerialized = this.rightChild.serialize();
		builder.setLeft(leftChildSerialized);
		builder.setRight(rightChildSerialized);
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
		// if  the hash must be recalculated.
		if(this.recalculateHash) {
			byte[] leftChildHash = this.leftChild.getHash();
			byte[] rightChildHash = this.rightChild.getHash();
			// commitment: H(leftChildHash || rightChildHash)
			byte[] commitment = new byte[leftChildHash.length+rightChildHash.length];
			System.arraycopy(leftChildHash, 0, commitment, 0, leftChildHash.length);
			System.arraycopy(rightChildHash, 0, commitment, leftChildHash.length, rightChildHash.length);
			this.hash = CryptographicDigest.digest(commitment);
			this.recalculateHash = false;
		}
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
	public void setLeftChild(Node leftChild) {
		this.leftChild = leftChild;
		this.changed = true;
		this.recalculateHash = true;
	}

	@Override
	public void setRightChild(Node rightChild) {
		this.rightChild = rightChild;
		this.changed = true;
		this.recalculateHash = true;
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

	@Override
	public void setValue(byte[] value) {
		throw new RuntimeException("tried to set value on an Interior Node");
	}

	@Override
	public boolean changed() {
		return this.changed;
	}

	@Override
	public void markChangedAll() {
		// because a node can only be changed
		// if its parent is changed, we 
		// can ignore entire subtrees to speed this up
		if(!this.leftChild.changed()) {
			this.leftChild.markChangedAll();
		}
		if(!this.rightChild.changed()) {
			this.rightChild.markChangedAll();
		}
	}

	@Override
	public void markUnchangedAll() {
		if(this.leftChild.changed()) {
			this.leftChild.markUnchangedAll();
		}
		if(this.rightChild.changed()) {
			this.rightChild.markUnchangedAll();
		}			
	}

}
