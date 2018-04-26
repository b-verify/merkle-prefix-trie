package mpt.dictionary;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;
import mpt.core.DictionaryLeafNode;
import mpt.core.EmptyLeafNode;
import mpt.core.InteriorNode;
import mpt.core.InvalidSerializationException;
import mpt.core.Node;
import mpt.core.Utils;
import serialization.MptSerialization;

/**
 * A Full Merkle Prefix Trie (MPT). This stores 
 * all mappings and authentication information. 
 * 
 *  Internally it contains NO STUBs and each node 
 *  tracks if it has been changed. Tracking changes 
 *  allow for lazy recalculation of hashes and 
 *  to keep track of updates. 
 *  
 *  MPT use structural equality
 *
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public class MPTDictionaryFull implements AuthenticatedDictionaryServer {

	private static final Logger LOGGER = Logger.getLogger(MPTDictionaryFull.class.getName());

	// we require that the root is always an interior node
	// at index -1, empty prefix (which I usually represent by +)
	protected InteriorNode root;

	/**
	 * Create an empty Merkle Prefix Trie
	 */
	public MPTDictionaryFull() {
		this.root = new InteriorNode(new EmptyLeafNode(), new EmptyLeafNode());
	}

	/**
	 * Create a Merkle Prefix Trie with the root. This constructor is private
	 * because it assumes that the internal structure of root is correct. This is
	 * not safe to expose to clients.
	 */
	private MPTDictionaryFull(InteriorNode root) {
		this.root = root;
	}

	@Override
	public void insert(final byte[] key, final byte[] value) {
		assert key.length == CryptographicDigest.getSizeBytes();
		assert value.length == CryptographicDigest.getSizeBytes();
		LOGGER.log(Level.FINE,
				"insert(" + Utils.byteArrayAsHexString(key) +") = " + Utils.byteArrayAsHexString(value));
		MPTDictionaryFull.insertHelper(key, value, -1, this.root);
	}

	private static Node insertHelper(final byte[] key, final byte[] value, 
			final int currentBitIndex, final Node currentNode) {
		// when we hit a leaf we know where we need to insert
		if (currentNode.isLeaf()) {
			// this key is already in the tree, update existing mapping
			if (Arrays.equals(currentNode.getKey(), key)) {
				// update the value
				currentNode.setValue(value);
				return currentNode;
			}
			// if the key is not in the tree add it
			DictionaryLeafNode nodeToAdd = new DictionaryLeafNode(key, value);
			if (currentNode.isEmpty()) {
				// if the current leaf is empty, just replace it
				return nodeToAdd;
			}
			// otherwise we need to "split"
			DictionaryLeafNode currentLeafNode = (DictionaryLeafNode) currentNode;
			// mark the current node as "changed" even though 
			// its value hasn't since it is now in a new location 
			// in the MPT
			currentLeafNode.markChangedAll();
			return MPTDictionaryFull.split(currentLeafNode, nodeToAdd, currentBitIndex);
		}
		boolean bit = Utils.getBit(key, currentBitIndex + 1);
		/*
		 * Encoding: if bit is 1 -> go right if bit is 0 -> go left
		 */
		if (bit) {
			Node newRightChild = MPTDictionaryFull.insertHelper(key, value, currentBitIndex + 1,
					currentNode.getRightChild());
			// update the right child
			currentNode.setRightChild(newRightChild);
			return currentNode;

		}
		Node newLeftChild = MPTDictionaryFull.insertHelper(key, value, currentBitIndex + 1, currentNode.getLeftChild());
		currentNode.setLeftChild(newLeftChild);
		return currentNode;
	}

	private static Node split(final DictionaryLeafNode a, final DictionaryLeafNode b, final int currentBitIndex) {
		assert !Arrays.equals(a.getKey(), b.getKey());
		boolean bitA = Utils.getBit(a.getKey(), currentBitIndex + 1);
		boolean bitB = Utils.getBit(b.getKey(), currentBitIndex + 1);
		// still collision, split again
		if (bitA == bitB) {
			// recursively split
			Node res;
			if (bitA) {
				// if bit is 1 add on the right
				res = MPTDictionaryFull.split(a, b, currentBitIndex + 1);
				return new InteriorNode(new EmptyLeafNode(), res);
			}
			// if bit is 0 add on the left
			res = split(a, b, currentBitIndex + 1);
			return new InteriorNode(res, new EmptyLeafNode());
		}
		// no collision
		if (bitA) {
			// bitA is 1, bitB is 0
			return new InteriorNode(b, a);
		}
		// bitA is 0, bitB is 1
		return new InteriorNode(a, b);
	}

	@Override
	public byte[] get(final byte[] key)  {
		assert key.length == CryptographicDigest.getSizeBytes();
		return MPTDictionaryFull.getHelper(this.root, key, -1);
	}

	private static byte[] getHelper(final Node currentNode, final byte[] key, final int currentBitIndex) {
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
			return MPTDictionaryFull.getHelper(currentNode.getRightChild(), key, currentBitIndex + 1);
		}
		return MPTDictionaryFull.getHelper(currentNode.getLeftChild(), key, currentBitIndex + 1);
	}

	@Override
	public void delete(final byte[] key) {
		assert key.length == CryptographicDigest.getSizeBytes();
		LOGGER.log(Level.FINE, "delete(" + Utils.byteArrayAsHexString(key) + ")");
		MPTDictionaryFull.deleteHelper(key, -1, this.root, true);
		// force updating the hash
		// TODO: later check if this is necessary and probably remove
		this.root.getHash();
	}

	private static Node deleteHelper(final byte[] key, final int currentBitIndex, final Node currentNode, 
			final boolean isRoot) {
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				if (Arrays.equals(currentNode.getKey(), key)) {
					return new EmptyLeafNode();
				}
			}
			// otherwise the key is not in the tree and nothing needs to be done
			return currentNode;
		}
		// we have to watch out to make sure that if this is the root node
		// that we return an InteriorNode and don't propagate up an empty node
		boolean bit = Utils.getBit(key, currentBitIndex + 1);
		Node leftChild = currentNode.getLeftChild();
		Node rightChild = currentNode.getRightChild();
		if (bit) {
			// delete key from the right subtree
			Node newRightChild = MPTDictionaryFull.deleteHelper(key, currentBitIndex + 1, rightChild, false);
			// if left subtree is empty, and rightChild is leaf
			// we push the newRightChild back up the MPT
			if (leftChild.isEmpty() && newRightChild.isLeaf() && !isRoot) {
				return newRightChild;
			}
			// if newRightChild is empty, and leftChild is a leaf
			// we push the leftChild back up the MPT
			if (newRightChild.isEmpty() && leftChild.isLeaf() && !isRoot) {
				// we also mark the left subtree as changed 
				// since its entire position has changed
				leftChild.markChangedAll();
				return leftChild;
			}
			// otherwise just update current (interior) node's
			// right child
			currentNode.setRightChild(newRightChild);
			return currentNode;
		}
		Node newLeftChild = MPTDictionaryFull.deleteHelper(key, currentBitIndex + 1, leftChild, false);
		if (rightChild.isEmpty() && newLeftChild.isLeaf() && !isRoot) {
			return newLeftChild;
		}
		if (newLeftChild.isEmpty() && rightChild.isLeaf() && !isRoot) {
			rightChild.markChangedAll();
			return rightChild;
		}
		currentNode.setLeftChild(newLeftChild);
		return currentNode;
	};

	@Override
	public byte[] commitment() {
		return this.root.getHash();
	};

	@Override
	public void reset() {
		this.root.markUnchangedAll();
	};
	
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidSerializationException("interior node does not have both children");
			}
			Node left = MPTDictionaryFull.parseNode(in.getLeft());
			Node right = MPTDictionaryFull.parseNode(in.getRight());
			return new InteriorNode(left, right);
		case STUB:
			throw new InvalidSerializationException("serialized full mpt should not have stubs");
		case LEAF:
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if (leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidSerializationException("dictionary leaf must have key and value");
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
	
	/**
	 * Deserialize a full MPT from bytes
	 * @param asbytes
	 * @return
	 * @throws InvalidSerializationException - if the serialization cannot be decoded
	 */
	public static MPTDictionaryFull deserialize(byte[] asbytes) throws InvalidSerializationException {
		MptSerialization.MerklePrefixTrie mpt;
		try {
			mpt = MptSerialization.MerklePrefixTrie.parseFrom(asbytes);
			return MPTDictionaryFull.deserialize(mpt);
		} catch (InvalidProtocolBufferException e) {
			throw new InvalidSerializationException(e.getMessage());
		}
	
	}
	
	/**
	 * Deserialzie a full MPT from a protobuf encoding
	 * @param mpt - a protobuf mpt encoding
	 * @return
	 * @throws InvalidSerializationException - if it cannot be parsed
	 */
	public static MPTDictionaryFull deserialize(MptSerialization.MerklePrefixTrie mpt) throws InvalidSerializationException {
		if (!mpt.hasRoot()) {
			throw new InvalidSerializationException("no root included");
		}
		Node root = MPTDictionaryFull.parseNode(mpt.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MPTDictionaryFull(rootInt);
	}

	public byte[] serialize() {
		MptSerialization.Node rootSerialization = this.root.serialize();
		MptSerialization.MerklePrefixTrie.Builder builder = MptSerialization.MerklePrefixTrie.newBuilder();
		builder.setRoot(rootSerialization);
		return builder.build().toByteArray();
	}

	/**
	 * Returns the height of the tree. Height is defined as the maximum possible
	 * distance from the leaf to the root node (TODO: I'm not sure this should be a
	 * public method - only really useful for benchmarking purposes)
	 * 
	 * @return
	 */
	public int getMaxHeight() {
		return this.getHeightRecursive(this.root);
	}

	private int getHeightRecursive(Node currentLocation) {
		// each leaf is at height zero
		if (currentLocation.isLeaf()) {
			return 0;
		}
		// otherwise we are at an interior node - height is maximum of
		// the height of the children plus 1
		return Math.max(this.getHeightRecursive(currentLocation.getLeftChild()),
				this.getHeightRecursive(currentLocation.getRightChild())) + 1;
	}

	@Override
	public String toString() {
		return "<MPTDictionaryFull \n"+MPTDictionaryFull.toStringHelper("+", this.root)+"\n>";
	}

	protected static String toStringHelper(String prefix, Node node) {
		String result = prefix + " " + node.toString();
		if (!node.isLeaf() && !node.isStub()) {
			String left = MPTDictionaryFull.toStringHelper(prefix + "0", node.getLeftChild());
			String right = MPTDictionaryFull.toStringHelper(prefix + "1", node.getRightChild());
			result = result + "\n" + left + "\n" + right;
		}
		return result;
	}

	/**
	 * Two MPTs are equal if they are STRUCTURALLY IDENTICAL which means two trees
	 * are equal if the contain identical nodes arranged in the same way.
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof MPTDictionaryFull) {
			MPTDictionaryFull othermpt = (MPTDictionaryFull) other;
			return this.root.equals(othermpt.root);
		}
		return false;
	}
}
