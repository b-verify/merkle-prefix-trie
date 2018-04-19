package mpt;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * TODO: make args final in recursive calls for clarity!
 */

/**
 * A Merkle Prefix Trie (MPT) to implement a Persistent Authenticated Dictionary
 *
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public class MerklePrefixTrie {

	private static final Logger LOGGER = Logger.getLogger(MerklePrefixTrie.class.getName());

	// we require that the root is always an interior node
	// at index -1, empty prefix (which I usually represent by +)
	protected InteriorNode root;

	/**
	 * Create an empty Merkle Prefix Trie
	 */
	public MerklePrefixTrie() {
		this.root = new InteriorNode(new EmptyLeafNode(), new EmptyLeafNode());
	}

	/**
	 * Create a Merkle Prefix Trie with the root. This constructor is private
	 * because it assumes that the internal structure of root is correct. This is
	 * not safe to expose to clients.
	 */
	private MerklePrefixTrie(InteriorNode root) {
		this.root = root;
	}

	public void insert(final byte[] key, final byte[] value) {
		LOGGER.log(Level.FINE,
				"insert(" + Utils.byteArrayAsHexString(key) + ", " + Utils.byteArrayAsHexString(value) + ")");
		byte[] keyHash = CryptographicDigest.digest(key);
		MerklePrefixTrie.insertHelper(key, value, keyHash, -1, this.root);
	}

	private static Node insertHelper(final byte[] key, final byte[] value, final byte[] keyHash, int currentBitIndex,
			Node currentNode) {
		// when we hit a leaf we know where we need to insert
		if (currentNode.isLeaf()) {
			// this key is already in the tree, update existing mapping
			if (Arrays.equals(currentNode.getKeyHash(), keyHash)) {
				// update the value
				currentNode.setValue(value);
				return currentNode;
			}
			// if the key is not in the tree add it
			LeafNode nodeToAdd = new LeafNode(key, value);
			if (currentNode.isEmpty()) {
				// if the current leaf is empty, just replace it
				return nodeToAdd;
			}
			// otherwise we need to "split"
			LeafNode currentLeafNode = (LeafNode) currentNode;
			// mark the current node as "changed" even though 
			// its value hasn't since it is now in a new location 
			// in the MPT
			currentLeafNode.markChangedAll();
			return MerklePrefixTrie.split(currentLeafNode, nodeToAdd, currentBitIndex);
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		/*
		 * Encoding: if bit is 1 -> go right if bit is 0 -> go left
		 */
		if (bit) {
			Node newRightChild = MerklePrefixTrie.insertHelper(key, value, keyHash, currentBitIndex + 1,
					currentNode.getRightChild());
			// update the right child
			currentNode.setRightChild(newRightChild);
			return currentNode;

		}
		Node newLeftChild = MerklePrefixTrie.insertHelper(key, value, keyHash, currentBitIndex + 1, currentNode.getLeftChild());
		currentNode.setLeftChild(newLeftChild);
		return currentNode;
	}

	/**
	 * Recursive helper function to split two leaves which are currently mapped to
	 * the same location the MPT (have the same prefix).
	 * 
	 * @param a
	 *            - the first leaf
	 * @param b
	 *            - the second leaf
	 * @param currentBitIndex
	 *            - a and b have the same prefix (collision) at least up to
	 *            currentBitIndex a.getKeyHash()[:currentBitIndex] ==
	 *            b.getKeyKash()[:currentBitIndex]
	 * @return
	 */
	private static Node split(LeafNode a, LeafNode b, int currentBitIndex) {
		assert !Arrays.equals(a.getKeyHash(), b.getKeyHash());
		boolean bitA = Utils.getBit(a.getKeyHash(), currentBitIndex + 1);
		boolean bitB = Utils.getBit(b.getKeyHash(), currentBitIndex + 1);
		// still collision, split again
		if (bitA == bitB) {
			// recursively split
			Node res;
			if (bitA) {
				// if bit is 1 add on the right
				res = MerklePrefixTrie.split(a, b, currentBitIndex + 1);
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

	public byte[] get(byte[] key)  {
		byte[] keyHash = CryptographicDigest.digest(key);
		return MerklePrefixTrie.getHelper(this.root, keyHash, -1);
	}

	private static byte[] getHelper(Node currentNode, byte[] keyHash, int currentBitIndex) {
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
			return MerklePrefixTrie.getHelper(currentNode.getRightChild(), keyHash, currentBitIndex + 1);
		}
		return MerklePrefixTrie.getHelper(currentNode.getLeftChild(), keyHash, currentBitIndex + 1);
	}

	public void delete(byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "delete(" + Utils.byteArrayAsHexString(key) + ")");
		MerklePrefixTrie.deleteHelper(keyHash, -1, this.root, true);
		// force updating the hash
		this.root.getHash();
	}

	private static Node deleteHelper(byte[] keyHash, int currentBitIndex, Node currentNode, boolean isRoot) {
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				if (Arrays.equals(currentNode.getKeyHash(), keyHash)) {
					return new EmptyLeafNode();
				}
			}
			// otherwise the key is not in the tree and nothing needs to be done
			return currentNode;
		}
		// we have to watch out to make sure that if this is the root node
		// that we return an InteriorNode and don't propagate up an empty node
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		Node leftChild = currentNode.getLeftChild();
		Node rightChild = currentNode.getRightChild();
		if (bit) {
			// delete key from the right subtree
			Node newRightChild = MerklePrefixTrie.deleteHelper(keyHash, currentBitIndex + 1, rightChild, false);
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
		Node newLeftChild = MerklePrefixTrie.deleteHelper(keyHash, currentBitIndex + 1, leftChild, false);
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

	public byte[] commitment() {
		return this.root.getHash();
	};

	public void reset() {
		this.root.markUnchangedAll();
	};
	
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws InvalidMPTSerializationException {
		switch (nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE:
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			if(!in.hasLeft() || !in.hasRight()) {
				throw new InvalidMPTSerializationException("interior node does not have both children");
			}
			Node left = MerklePrefixTrie.parseNode(in.getLeft());
			Node right = MerklePrefixTrie.parseNode(in.getRight());
			return new InteriorNode(left, right);
		case STUB:
			throw new InvalidMPTSerializationException("serialized full mpt should not have stubs");
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
	
	public static MerklePrefixTrie deserialize(byte[] asbytes) throws InvalidMPTSerializationException {
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
		Node root = MerklePrefixTrie.parseNode(mpt.getRoot());
		if (!(root instanceof InteriorNode)) {
			throw new InvalidMPTSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MerklePrefixTrie(rootInt);
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
		return MerklePrefixTrie.toStringHelper("+", this.root);
	}

	protected static String toStringHelper(String prefix, Node node) {
		String result = prefix + " " + node.toString();
		if (!node.isLeaf() && !node.isStub()) {
			String left = MerklePrefixTrie.toStringHelper(prefix + "0", node.getLeftChild());
			String right = MerklePrefixTrie.toStringHelper(prefix + "1", node.getRightChild());
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
		if (other instanceof MerklePrefixTrie) {
			MerklePrefixTrie othermpt = (MerklePrefixTrie) other;
			return this.root.equals(othermpt.root);
		}
		return false;
	}
}
