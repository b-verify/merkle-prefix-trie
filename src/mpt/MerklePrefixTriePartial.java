package mpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * Partial Merkle Prefix Tries contain a subset of the information 
 * of the Full Merkle Prefix Trie. The omitted portions are tracked internally
 * with "Stubs" which only store the hash of the omitted portion. Because
 * they contain a subset of the authentication information, 
 * the partial Merkle Prefix Trie can only support some operations and 
 * may not have enough information for others. 
 * 
 * @author henryaspegren
 *
 */
public class MerklePrefixTriePartial implements AuthenticatedDictionaryClient {
	
	private InteriorNode root;

	/**
	 * Create a partial MPT from the full MPT. Since no keys are provided 
	 * this just copies the root.
	 * @param fullMPT - full MPT to copy from
	 */
	public MerklePrefixTriePartial(MerklePrefixTrieFull fullMPT) {
		// just copies the root
		this.root = new InteriorNode(new Stub(fullMPT.root.getLeftChild().getHash()),
				new Stub(fullMPT.root.getRightChild().getHash()));
	}
	
	/**
	 * Create a partial MPT from the full MPT such that 
	 * the partial contains specified key mapping 
	 * (if the mapping exists and a path to a leaf if it 
	 * does not) and authentication information from the 
	 * full MPT.
	 * 
	 * @param fullMPT - the full MPT to copy the key mapping
	 * and authentication information from.
	 * @param key - the key to copy
	 */
	public MerklePrefixTriePartial(MerklePrefixTrieFull fullMPT, byte[] key) {
		List<byte[]> keyHashes = new ArrayList<>();
		keyHashes.add(CryptographicDigest.digest(key));
		Node root = MerklePrefixTriePartial.copyMultiplePaths(keyHashes, fullMPT.root, -1);
		this.root = (InteriorNode) root;
	}
	
	/**
	 * Create a partial MPT from the full MPT such that 
	 * the partial contains the specified key mappings 
	 * (if the key exists and a path to a leaf if it does not) 
	 * along with the required authentication information. 
	 * @param fullMPT - the full MPT to copy mappings and authentication 
	 * information from 
	 * @param keys - the key mappings to copy
	 */
	public MerklePrefixTriePartial(MerklePrefixTrieFull fullMPT, List<byte[]> keys) {
		List<byte[]> keyHashes = new ArrayList<>();
		for(byte[] key : keys) {
			keyHashes.add(CryptographicDigest.digest(key));
		}
		Node root = MerklePrefixTriePartial.copyMultiplePaths(keyHashes, fullMPT.root, -1);
		this.root = (InteriorNode) root;
	}
	
	private MerklePrefixTriePartial(InteriorNode root) {
		this.root = root;
	}
	
	private static Node copyMultiplePaths(final List<byte[]> matchingKeyHashes, final Node copyNode, final int currentBitIndex) {
		// case: if this is not on the path to the key hash 
		if(matchingKeyHashes.size() == 0) {
			if(copyNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			return new Stub(copyNode.getHash());
		}
		// case: if this is on the path to a key hash
		// subcase: if we are at the end of a path
		if(copyNode.isLeaf()) {
			if(copyNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			return new LeafNode(copyNode.getKey(), copyNode.getValue());
		}
		// subcase: intermediate node
		
		// divide up keys into those that match the right prefix (...1)
		// and those that match the left prefix (...0)
		List<byte[]> matchRight = new ArrayList<byte[]>();
		List<byte[]> matchLeft = new ArrayList<byte[]>();
		for(byte[] keyHash : matchingKeyHashes) {
			final boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
			if(bit) {
				matchRight.add(keyHash);
			}else {
				matchLeft.add(keyHash);
			}
		}
		Node leftChild = MerklePrefixTriePartial.copyMultiplePaths(matchLeft, copyNode.getLeftChild(), currentBitIndex+1);
		Node rightChild = MerklePrefixTriePartial.copyMultiplePaths(matchRight, copyNode.getRightChild(), currentBitIndex+1);
		return new InteriorNode(leftChild, rightChild);
	}
	
	public byte[] get(final byte[] key) throws InsufficientAuthenticationDataException {
		byte[] keyHash = CryptographicDigest.digest(key);
		return MerklePrefixTriePartial.getHelper(this.root, keyHash, -1);
	}

	private static byte[] getHelper(final Node currentNode, final byte[] keyHash, final int currentBitIndex) 
			throws InsufficientAuthenticationDataException {
		if (currentNode.isStub()) {
			throw new InsufficientAuthenticationDataException(
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
	
	/**
	 * Deserialize a partial MPT from bytes
	 * @param asbytes
	 * @return
	 * @throws InvalidSerializationException - if the serialization cannot be decoded
	 */
	public static MerklePrefixTriePartial deserialize(byte[] asbytes) throws InvalidSerializationException {
		MptSerialization.MerklePrefixTrie mpt;
		try {
			mpt = MptSerialization.MerklePrefixTrie.parseFrom(asbytes);
		} catch (InvalidProtocolBufferException e) {
			throw new InvalidSerializationException(e.getMessage());
		}
		if (!mpt.hasRoot()) {
			throw new InvalidSerializationException("no root included");
		}
		// when we deserialize a full MPT we do not use any cached values
		Node root = MerklePrefixTriePartial.parseNode(mpt.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MerklePrefixTriePartial(rootInt);
	}
	
	@Override
	public void processUpdates(MptSerialization.MerklePrefixTrie updates) throws InvalidSerializationException {
		if(!updates.hasRoot()) {
			throw new InvalidSerializationException("update has no root");
		}
		Node newRoot = MerklePrefixTriePartial.parseNodeUsingCachedValues(this.root, updates.getRoot());
		this.root = (InteriorNode) newRoot;
	}
	
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidSerializationException("interior node does not have both children");
			}
			Node left = MerklePrefixTriePartial.parseNode(in.getLeft());
			Node right = MerklePrefixTriePartial.parseNode(in.getRight());
			return new InteriorNode(left, right);
		case STUB:
			MptSerialization.Stub stub = nodeSerialization.getStub();
			if (stub.getHash().isEmpty()) {
				throw new InvalidSerializationException("stub doesn't have a hash");
			}
			return new Stub(stub.getHash().toByteArray());
		case LEAF:
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if (leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidSerializationException("leaf doesn't have required keyhash and value");
			}
			return new LeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case EMPTYLEAF:
			return new EmptyLeafNode();
		case NODE_NOT_SET:
			throw new InvalidSerializationException("no node included - fatal error");
		default:
			throw new InvalidSerializationException("?????");
		}
	}
	
	private static Node parseNodeUsingCachedValues(Node currentNode, MptSerialization.Node updatedNode)
			throws InvalidSerializationException {
		switch(updatedNode.getNodeCase()) {
		case EMPTYLEAF:
			return new EmptyLeafNode();
		case INTERIOR_NODE:
			// the case here requires more care, since a child might be omitted, 
			// in which case the client should use the current value (this 
			// is a caching scheme for efficiency)
			MptSerialization.InteriorNode interiorNode = updatedNode.getInteriorNode();
			Node left = null;
			Node right = null;
			if(currentNode != null) {
				left = currentNode.getLeftChild();
				right = currentNode.getRightChild();	
			}
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
			throw new InvalidSerializationException("tried to use a cached node that is not present");
		default:
			throw new InvalidSerializationException("?????");
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
		return "<MerklePrefixTriePartial \n"+MerklePrefixTrieFull.toStringHelper("+", this.root)+"\n>";
	}
}
