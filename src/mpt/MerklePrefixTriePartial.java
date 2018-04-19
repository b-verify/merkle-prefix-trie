package mpt;

import java.util.Arrays;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

public class MerklePrefixTriePartial {
	
	private InteriorNode root;

	public MerklePrefixTriePartial(MerklePrefixTrie fullMPT) {
		// just copies the root
		this.root = new InteriorNode(new Stub(fullMPT.root.getLeftChild().getHash()),
				new Stub(fullMPT.root.getRightChild().getHash()));
	}
	
	private MerklePrefixTriePartial(InteriorNode root) {
		this.root = root;
	}
	
	public void addPath(MerklePrefixTrie fullMPT, byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		System.out.println("adding path for: "+Utils.byteArrayAsBitString(keyHash));
		Node newRoot = MerklePrefixTriePartial.addPathHelper(this.root, fullMPT.root, keyHash, -1);
		this.root = (InteriorNode) newRoot;
	}
	
	private static Node addPathHelper(final Node thisNode, 
			final Node copyNode, final byte[] keyHash, int currentBitIndex) {
		System.out.println("this node: "+thisNode+" copy node: "+copyNode);
		if(copyNode.isLeaf()) {
			if(copyNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			return new LeafNode(copyNode.getKey(), copyNode.getValue());				
		}
		Node leftChild = copyNode.getLeftChild();
		Node rightChild = copyNode.getRightChild();
		Node thisLeftChild = null;
		Node thisRightChild = null;
		if(thisNode != null) {
			if(!thisNode.isStub()) {
				thisLeftChild = thisNode.getLeftChild();
				thisRightChild = thisNode.getRightChild();
			}
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		if(bit) {
			rightChild = MerklePrefixTriePartial.addPathHelper(
					thisRightChild, copyNode.getRightChild(), keyHash, currentBitIndex+1);
			// use this left child if we have one 
			if(thisLeftChild != null) {
				// and it is not a stub 
				if(!thisLeftChild.isStub()) {
					return new InteriorNode(thisLeftChild, rightChild);					
				}
			}
			return new InteriorNode(new Stub(leftChild.getHash()), rightChild);
		}
		leftChild = MerklePrefixTriePartial.addPathHelper(
				thisLeftChild, copyNode.getLeftChild(), keyHash, currentBitIndex+1); 
		if(thisRightChild != null) {
			if(!thisRightChild.isStub()) {
				return new InteriorNode(leftChild, thisRightChild);			
			}
		}
		return new InteriorNode(leftChild, new Stub(rightChild.getHash()));
	}
	
	public byte[] get(byte[] key) throws IncompleteMPTException {
		byte[] keyHash = CryptographicDigest.digest(key);
		return MerklePrefixTriePartial.getHelper(this.root, keyHash, -1);
	}

	private static byte[] getHelper(Node currentNode, byte[] keyHash, int currentBitIndex) throws IncompleteMPTException {
		if (currentNode.isStub()) {
			throw new IncompleteMPTException(
					"stub encountered at: " + Utils.byteArrayPrefixAsBitString(keyHash, currentBitIndex));
		}
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				// if the current node is NonEmpty and matches the Key
				if (Arrays.equals(currentNode.getKeyHash(), keyHash)) {
					return currentNode.getValue();
				}
			}
			// otherwise key not in the MPT - return null;
			return null;
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		if (bit) {
			return MerklePrefixTriePartial.getHelper(currentNode.getRightChild(), keyHash, currentBitIndex + 1);
		}
		return MerklePrefixTriePartial.getHelper(currentNode.getLeftChild(), keyHash, currentBitIndex + 1);
	}
	
	public byte[] commitment() {
		return this.root.getHash();
	}
	
	public static MerklePrefixTriePartial deserialize(byte[] asbytes) throws InvalidMPTSerializationException {
		MptSerialization.MerklePrefixTrie mpt;
		try {
			mpt = MptSerialization.MerklePrefixTrie.parseFrom(asbytes);
		} catch (InvalidProtocolBufferException e) {
			throw new InvalidMPTSerializationException(e.getMessage());
		}
		if (!mpt.hasRoot()) {
			throw new InvalidMPTSerializationException("no root included");
		}
		// when we deserialize a full MPT we do not use any cached values
		Node root = MerklePrefixTriePartial.parseNode(mpt.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidMPTSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MerklePrefixTriePartial(rootInt);
	}
	
	public void deserializeUpdates(byte[] updateBytes) throws InvalidMPTSerializationException {
		try {
			MptSerialization.MerklePrefixTrie mptUpdate = MptSerialization.MerklePrefixTrie.parseFrom(updateBytes);
			// when we deserialize updates to a MPT, 
			// the current values are cached and omitted in updates!
			Node newRoot = MerklePrefixTriePartial.parseNodeUsingCachedValues(this.root, mptUpdate.getRoot());
			this.root = (InteriorNode) newRoot;
		} catch (InvalidProtocolBufferException e) {
			throw new InvalidMPTSerializationException(e.getMessage());
		}
	}
	
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidMPTSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidMPTSerializationException("interior node does not have both children");
			}
			Node left = MerklePrefixTriePartial.parseNode(in.getLeft());
			Node right = MerklePrefixTriePartial.parseNode(in.getRight());
			return new InteriorNode(left, right);
		case STUB:
			MptSerialization.Stub stub = nodeSerialization.getStub();
			if (stub.getHash().isEmpty()) {
				throw new InvalidMPTSerializationException("stub doesn't have a hash");
			}
			return new Stub(stub.getHash().toByteArray());
		case LEAF:
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if (leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidMPTSerializationException("leaf doesn't have required keyhash and value");
			}
			return new LeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case EMPTYLEAF:
			return new EmptyLeafNode();
		case NODE_NOT_SET:
			throw new InvalidMPTSerializationException("no node included - fatal error");
		default:
			throw new InvalidMPTSerializationException("?????");
		}
	}
	
	private static Node parseNodeUsingCachedValues(Node currentNode, MptSerialization.Node updatedNode)
			throws InvalidMPTSerializationException {
		switch(updatedNode.getNodeCase()) {
		case EMPTYLEAF:
			return new EmptyLeafNode();
		case INTERIOR_NODE:
			// the case here requires more care, since a child might be omitted, 
			// in which case the client should use the current value (this 
			// is a caching scheme for efficiency)
			MptSerialization.InteriorNode interiorNode = updatedNode.getInteriorNode();
			Node left = currentNode.getLeftChild();
			Node right = currentNode.getRightChild();
			if(interiorNode.hasLeft()) {
				left = MerklePrefixTriePartial.parseNodeUsingCachedValues(left, interiorNode.getLeft());
			}
			if(interiorNode.hasRight()) {
				right = MerklePrefixTriePartial.parseNodeUsingCachedValues(right, interiorNode.getRight());
			}
			return new InteriorNode(left, right);
		case LEAF:
			MptSerialization.Leaf leaf = updatedNode.getLeaf();
			return new LeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case STUB:
			MptSerialization.Stub stub = updatedNode.getStub();
			return new Stub(stub.getHash().toByteArray());
		case NODE_NOT_SET:
			throw new InvalidMPTSerializationException("tried to use a cached node that is not present");
		default:
			throw new InvalidMPTSerializationException("?????");
		}
	}
	
	public byte[] serialize() {
		MptSerialization.Node rootSerialization = this.root.serialize();
		MptSerialization.MerklePrefixTrie.Builder builder = MptSerialization.MerklePrefixTrie.newBuilder();
		builder.setRoot(rootSerialization);
		return builder.build().toByteArray();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof MerklePrefixTriePartial) {
			MerklePrefixTriePartial otherPartialMPT = (MerklePrefixTriePartial) other;
			return otherPartialMPT.root.equals(this.root);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "<MerklePrefixTriePartial \n"+MerklePrefixTrie.toStringHelper("+", this.root)+"\n>";
	}
}
