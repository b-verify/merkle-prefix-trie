package mpt.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import crypto.CryptographicDigest;
import serialization.generated.MptSerialization;

/**
 * (MUTABLE) 
 * 
 * Represents an interior node in the MPT. An interior node has 
 * two children, a left child and right child. Interior nodes do not store 
 * keys or values. The hash of the interior node is H(left.getHash()||right.getHash())
 * where left.getHash() (resp. right.getHash()) is the hash of the left (resp right) 
 * child.
 * 
 * The children of the interior node may be changed. Whenever the children are changed
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
	
	private static final String INTERIOR_NODE_MSG = new String("<InterirorNode>");
	
	public InteriorNode(Node leftChild, Node rightChild) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.changed = true;
		this.recalculateHash = true;
	}
	
	public MptSerialization.Node serialize() {
		MptSerialization.InteriorNode.Builder builder = MptSerialization.InteriorNode.newBuilder();
		serialization.generated.MptSerialization.Node leftChildSerialized = this.leftChild.serialize();
		serialization.generated.MptSerialization.Node rightChildSerialized = this.rightChild.serialize();
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
		// if the hash must be recalculated.
		if(this.recalculateHash) {
			byte[] leftChildHash = this.leftChild.getHash();
			byte[] rightChildHash = this.rightChild.getHash();
			// commitment: H(leftChildHash || rightChildHash)
			byte[] commitment = new byte[leftChildHash.length+rightChildHash.length];
			System.arraycopy(leftChildHash, 0, commitment, 0, leftChildHash.length);
			System.arraycopy(rightChildHash, 0, commitment, leftChildHash.length, rightChildHash.length);
			this.hash = CryptographicDigest.hash(commitment);
			this.recalculateHash = false;
		}
		return this.hash.clone();
	}
	
	public byte[] getHashParallel(ExecutorService executor) {
		// if  the hash must be recalculated.
		if(this.recalculateHash) {
			Callable<byte[]> leftTask = () -> {
				return this.leftChild.getHash();
			};			
			Callable<byte[]> rightTask = () -> {
				return this.rightChild.getHash();
			};
			Future<byte[]> leftTaskRes = executor.submit(leftTask);
			Future<byte[]> rightTaskRes = executor.submit(rightTask);
			
			byte[] leftChildHash;
			byte[] rightChildHash;
			try {
				rightChildHash = rightTaskRes.get();
				leftChildHash = leftTaskRes.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			
			// commitment: H(leftChildHash || rightChildHash)
			byte[] commitment = new byte[leftChildHash.length+rightChildHash.length];
			System.arraycopy(leftChildHash, 0, commitment, 0, leftChildHash.length);
			System.arraycopy(rightChildHash, 0, commitment, leftChildHash.length, rightChildHash.length);
			this.hash = CryptographicDigest.hash(commitment);
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
		return InteriorNode.INTERIOR_NODE_MSG;
	}

	@Override
	public byte[] getKey() {
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
		
		this.changed = false;
	}
	
	@Override
	public int countHashesRequiredForGetHash() {
		if(this.recalculateHash) {
			int total = 1+this.leftChild.countHashesRequiredForGetHash()+this.rightChild.countHashesRequiredForGetHash();
			return total;
		}
		return 0;
	}
	
	@Override
	public int nodesInSubtree() {
		return 1+this.rightChild.nodesInSubtree()+this.leftChild.nodesInSubtree();
	}

	@Override
	public int interiorNodesInSubtree() {
		return 1+this.rightChild.interiorNodesInSubtree()+this.leftChild.interiorNodesInSubtree();
	}

	@Override
	public int emptyLeafNodesInSubtree() {
		return this.rightChild.emptyLeafNodesInSubtree()+this.leftChild.emptyLeafNodesInSubtree();
	}
	
	@Override
	public int nonEmptyLeafNodesInSubtree() {
		return this.rightChild.nonEmptyLeafNodesInSubtree()+this.leftChild.nonEmptyLeafNodesInSubtree();
	}

}
