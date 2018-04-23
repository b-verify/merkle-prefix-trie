package mpt.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;
import mpt.core.EmptyLeafNode;
import mpt.core.InsufficientAuthenticationDataException;
import mpt.core.InteriorNode;
import mpt.core.InvalidSerializationException;
import mpt.core.DictionaryLeafNode;
import mpt.core.Node;
import mpt.core.SetLeafNode;
import mpt.core.Stub;
import mpt.core.Utils;
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
public class MPTSetPartial implements AuthenticatedSetClient {
	
	private InteriorNode root;

	/**
	 * Create a partial MPT set from the full MPT set. Since no 
	 * values are specified just copies the root.
	 * @param fullMPTSet - full MPT set to copy from
	 */
	public MPTSetPartial(MPTSetFull fullMPTSet) {
		// just copies the root
		this.root = new InteriorNode(new Stub(fullMPTSet.root.getLeftChild().getHash()),
				new Stub(fullMPTSet.root.getRightChild().getHash()));
	}
	
	/**
	 * Create a partial MPT set from the full MPT set such that 
	 * the partial contains the mappings to the specified values
	 * (if the mapping exists and a path to a leaf if it 
	 * does not) and authentication information from the 
	 * full MPT set.
	 * 
	 * @param fullMPTSet - the full MPT set to copy the key mapping
	 * and authentication information from.
	 * @param value - the value to copy
	 */
	public MPTSetPartial(MPTSetFull fullMPTSet, byte[] value) {
		List<byte[]> valueHashes = new ArrayList<>();
		valueHashes.add(CryptographicDigest.hash(value));
		Node root = MPTSetPartial.copyMultiplePaths(valueHashes, fullMPTSet.root, -1);
		this.root = (InteriorNode) root;
	}
	
	/**
	 * Create a partial MPT from the full MPT such that 
	 * the partial contains the specified key mappings 
	 * (if the key exists and a path to a leaf if it does not) 
	 * along with the required authentication information. 
	 * @param fullMPTSet - the full MPT set 
	 * to copy mappings and authentication information from 
	 * @param keys - the key mappings to copy
	 */
	public MPTSetPartial(MPTSetFull fullMPTSet, List<byte[]> values) {
		List<byte[]> valueHashes = new ArrayList<>();
		for(byte[] value : values) {
			valueHashes.add(CryptographicDigest.hash(value));
		}
		Node root = MPTSetPartial.copyMultiplePaths(valueHashes, fullMPTSet.root, -1);
		this.root = (InteriorNode) root;
	}
	
	private static Node copyMultiplePaths(final List<byte[]> matchingValueHashes, final Node copyNode, final int currentBitIndex) {
		// case: if this is not on the path 
		if(matchingValueHashes.size() == 0) {
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
		for(byte[] valueHash : matchingValueHashes) {
			final boolean bit = Utils.getBit(valueHash, currentBitIndex + 1);
			if(bit) {
				matchRight.add(valueHash);
			}else {
				matchLeft.add(valueHash);
			}
		}
		Node leftChild = MPTSetPartial.copyMultiplePaths(matchLeft, copyNode.getLeftChild(), currentBitIndex+1);
		Node rightChild = MPTSetPartial.copyMultiplePaths(matchRight, copyNode.getRightChild(), currentBitIndex+1);
		return new InteriorNode(leftChild, rightChild);
	}
	
	private MPTSetPartial(InteriorNode root) {
		this.root = root;
	}
	
	@Override
	public boolean inSet(final byte[] value) throws InsufficientAuthenticationDataException {
		byte[] valueHash = CryptographicDigest.hash(value);
		return MPTSetPartial.getHelper(this.root, valueHash, -1);
	}

	private static boolean getHelper(final Node currentNode, final byte[] valueHash, final int currentBitIndex) 
			throws InsufficientAuthenticationDataException {
		if (currentNode.isStub()) {
			throw new InsufficientAuthenticationDataException(
					"stub encountered at: " + Utils.byteArrayPrefixAsBitString(valueHash, currentBitIndex));
		}
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				// if value in MPT
				if (Arrays.equals(currentNode.getKeyHash(), valueHash)) {
					return true;
				}
			}
			// otherwise value not in the MPT
			return false;
		}
		boolean bit = Utils.getBit(valueHash, currentBitIndex + 1);
		if (bit) {
			return MPTSetPartial.getHelper(currentNode.getRightChild(), valueHash, currentBitIndex + 1);
		}
		return MPTSetPartial.getHelper(currentNode.getLeftChild(), valueHash, currentBitIndex + 1);
	}
	
	@Override
	public byte[] commitment() {
		return this.root.getHash();
	}
	
	/**
	 * Deserialize a partial MPT set from bytes
	 * @param asbytes
	 * @return
	 * @throws InvalidSerializationException - if the serialization cannot be decoded
	 */
	public static MPTSetPartial deserialize(byte[] asbytes) throws InvalidSerializationException {
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
		Node root = MPTSetPartial.parseNode(mpt.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MPTSetPartial(rootInt);
	}
		
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidSerializationException("interior node does not have both children");
			}
			Node left = MPTSetPartial.parseNode(in.getLeft());
			Node right = MPTSetPartial.parseNode(in.getRight());
			return new InteriorNode(left, right);
		case STUB:
			MptSerialization.Stub stub = nodeSerialization.getStub();
			if (stub.getHash().isEmpty()) {
				throw new InvalidSerializationException("stub doesn't have a hash");
			}
			return new Stub(stub.getHash().toByteArray());
		case LEAF:
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if (!leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidSerializationException("set leaf should only have a value");
			}
			return new SetLeafNode(leaf.getValue().toByteArray());
		case EMPTYLEAF:
			return new EmptyLeafNode();
		case NODE_NOT_SET:
			throw new InvalidSerializationException("no node included - fatal error");
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
		if(other instanceof MPTSetPartial) {
			MPTSetPartial otherPartialMPT = (MPTSetPartial) other;
			return otherPartialMPT.root.equals(this.root);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "<MPTSetPartial \n"+MPTSetFull.toStringHelper("+", this.root)+"\n>";
	}

}
