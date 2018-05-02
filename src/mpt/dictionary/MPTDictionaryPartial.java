package mpt.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import crypto.CryptographicDigest;
import mpt.core.EmptyLeafNode;
import mpt.core.InsufficientAuthenticationDataException;
import mpt.core.InteriorNode;
import mpt.core.InvalidSerializationException;
import mpt.core.DictionaryLeafNode;
import mpt.core.Node;
import mpt.core.Stub;
import mpt.core.Utils;
import serialization.generated.MptSerialization;

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
public class MPTDictionaryPartial implements AuthenticatedDictionaryClient {
	
	//protected InteriorNode root;
	protected Node root; 

	/**
	 * Create a partial MPT from the full MPT. Since no keys are provided 
	 * this just copies the root.
	 * @param fullMPT - full MPT to copy from
	 */
	public MPTDictionaryPartial(MPTDictionaryFull fullMPT) {
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
	public MPTDictionaryPartial(MPTDictionaryFull fullMPT, byte[] key) {
		assert key.length == CryptographicDigest.getSizeBytes();
		List<byte[]> keys = new ArrayList<>();
		keys.add(key);
		Node root = MPTDictionaryPartial.copyMultiplePaths(keys, fullMPT.root, -1);
		//this.root = (InteriorNode) root;
		this.root = root;
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
	public MPTDictionaryPartial(MPTDictionaryFull fullMPT, List<byte[]> keys) {
		for(byte[] key : keys) {
			assert key.length == CryptographicDigest.getSizeBytes();
		}
		Node root = MPTDictionaryPartial.copyMultiplePaths(keys, fullMPT.root, -1);
		//Node root = MPTDictionaryPartial.copyMultiplePathsRoot(keys, fullMPT.root, -1);
		//this.root = (InteriorNode) root;
		this.root = root;
	}
	
	private MPTDictionaryPartial(InteriorNode root) {
		this.root = root;
	}
	
	private static Node copyMultiplePathsRoot(final List<byte[]> matchingKeys, final Node copyNode, final int currentBitIndex) {
		Node leftChild = copyMultiplePaths(matchingKeys, copyNode.getLeftChild(), 0);
		Node rightChild = copyMultiplePaths(matchingKeys, copyNode.getRightChild(), 0);
		return new InteriorNode(leftChild, rightChild);
	}
		
	
	private static Node copyMultiplePaths(final List<byte[]> matchingKeys, final Node copyNode, final int currentBitIndex) {
		// case: if this is not on the path to the key hash 
		if(matchingKeys.size() == 0) {
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
			return new DictionaryLeafNode(copyNode.getKey(), copyNode.getValue());
		}
		// subcase: intermediate node
		
		// divide up keys into those that match the right prefix (...1)
		// and those that match the left prefix (...0)
		List<byte[]> matchRight = new ArrayList<byte[]>();
		List<byte[]> matchLeft = new ArrayList<byte[]>();
		for(byte[] key : matchingKeys) {
			final boolean bit = Utils.getBit(key, currentBitIndex + 1);
			if(bit) {
				matchRight.add(key);
			}else {
				matchLeft.add(key);
			}
		}
		Node leftChild = MPTDictionaryPartial.copyMultiplePaths(matchLeft, copyNode.getLeftChild(), currentBitIndex+1);
		Node rightChild = MPTDictionaryPartial.copyMultiplePaths(matchRight, copyNode.getRightChild(), currentBitIndex+1);
		return new InteriorNode(leftChild, rightChild);
	}
	
	@Override
	public byte[] get(final byte[] key) throws InsufficientAuthenticationDataException {
		assert key.length == CryptographicDigest.getSizeBytes();
		return MPTDictionaryPartial.getHelper(this.root, key, -1);
	}

	private static byte[] getHelper(final Node currentNode, final byte[] key, final int currentBitIndex) 
			throws InsufficientAuthenticationDataException {
		if (currentNode.isStub()) {
			throw new InsufficientAuthenticationDataException(
					"stub encountered at: " + Utils.byteArrayPrefixAsBitString(key, currentBitIndex));
		}
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				// if the current node is NonEmpty and matches the Key
				if (Arrays.equals(currentNode.getKey(), key)) {
					return currentNode.getValue();
				}
			}
			// otherwise key not in the MPT - return null;
			return null;
		}
		boolean bit = Utils.getBit(key, currentBitIndex + 1);
		if (bit) {
			return MPTDictionaryPartial.getHelper(currentNode.getRightChild(), key, currentBitIndex + 1);
		}
		return MPTDictionaryPartial.getHelper(currentNode.getLeftChild(), key, currentBitIndex + 1);
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
	public static MPTDictionaryPartial deserialize(byte[] asbytes) throws InvalidSerializationException {
		MptSerialization.MerklePrefixTrie mpt;
		try {
			mpt = MptSerialization.MerklePrefixTrie.parseFrom(asbytes);
			return MPTDictionaryPartial.deserialize(mpt);
		} catch (InvalidProtocolBufferException e) {
			throw new InvalidSerializationException(e.getMessage());
		}
	}
	
	/**
	 * Deserialize a partial MPT from the protobuf representation
	 * @param partialMPT - partial MPT protobuf
	 * @return
	 * @throws InvalidSerializationException - if it cannot properly be decoded
	 */
	public static MPTDictionaryPartial deserialize(MptSerialization.MerklePrefixTrie partialMPT) throws 
		InvalidSerializationException {
		if(!partialMPT.hasRoot()) {
			throw new InvalidSerializationException("no root included");
		}
		// when we deserialize a full MPT we do not use any cached values
		Node root = MPTDictionaryPartial.parseNode(partialMPT.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MPTDictionaryPartial(rootInt);
	}
	
	@Override
	public void processUpdates(MptSerialization.MerklePrefixTrie updates) throws InvalidSerializationException {
		if(!updates.hasRoot()) {
			throw new InvalidSerializationException("update has no root");
		}
		Node newRoot = MPTDictionaryPartial.parseNodeUsingCachedValues(this.root, updates.getRoot());
		System.out.println(updates.getRoot());
		//this.root = (InteriorNode) newRoot;
		this.root = newRoot;
	}
	
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidSerializationException("interior node does not have both children");
			}
			Node left = MPTDictionaryPartial.parseNode(in.getLeft());
			Node right = MPTDictionaryPartial.parseNode(in.getRight());
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
			return new DictionaryLeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
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
			System.out.println(updatedNode);
			System.out.println("is empty leaf");
			return new EmptyLeafNode();
		case INTERIOR_NODE:
			System.out.println(updatedNode);
			System.out.println("is interior node");
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
				left = MPTDictionaryPartial.parseNodeUsingCachedValues(left, interiorNode.getLeft());
			}
			if(interiorNode.hasRight()) {
				right = MPTDictionaryPartial.parseNodeUsingCachedValues(right, interiorNode.getRight());
			}
			return new InteriorNode(left, right);
		case LEAF:
			System.out.println(updatedNode);
			System.out.println("is leaf");
			MptSerialization.Leaf leaf = updatedNode.getLeaf();
			return new DictionaryLeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case STUB:
			System.out.println(updatedNode);
			System.out.println("is STUB");
			MptSerialization.Stub stub = updatedNode.getStub();
			return new Stub(stub.getHash().toByteArray());
		case NODE_NOT_SET:
			throw new InvalidSerializationException("tried to use a cached node that is not present");
		default:
			throw new InvalidSerializationException("?????");
		}
	}
	
	@Override
	public MptSerialization.MerklePrefixTrie serialize() {
		MptSerialization.Node rootSerialization = this.root.serialize();
		MptSerialization.MerklePrefixTrie.Builder builder = MptSerialization.MerklePrefixTrie.newBuilder();
		builder.setRoot(rootSerialization);
		return builder.build();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof MPTDictionaryPartial) {
			MPTDictionaryPartial otherPartialMPT = (MPTDictionaryPartial) other;
			return otherPartialMPT.root.equals(this.root);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "<MPTDictionaryPartial \n"+MPTDictionaryFull.toStringHelper("+", this.root)+"\n>";
	}
}
