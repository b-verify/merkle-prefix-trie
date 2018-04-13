package mpt;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;

import serialization.MptSerialization;

/**
 * A  Merkle Prefix Trie (MPT) 
 * 
 * TODO: add an interface and move the public methods and specs to the interface file
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public class MerklePrefixTrie {
	
	private static final Logger LOGGER = Logger.getLogger( MerklePrefixTrie.class.getName() );
	private Node root;
	
	/**
	 * Create an empty Merkle Prefix Trie
	 */
	public MerklePrefixTrie() {
		this.root = new InteriorNode(new EmptyLeafNode(), new EmptyLeafNode());
	}
	
	/**
	 * Create a Merkle Prefix Trie with the root. This constructor is 
	 * private because it assumes that the internal structure of root is correct. 
	 * This is not safe to expose to clients
	 */
	private MerklePrefixTrie(InteriorNode root) {
		this.root = root;
	}
	
	/**
	 * Add a (key, value) mapping to the MPT. 
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return true if the trie was modified (i.e. if the value for the key was updated)
	 */
	public boolean set(byte[] key, byte[] value){
		LeafNode leafNodeToAdd = new LeafNode(key, value);
		LOGGER.log(Level.FINE, "set("+MerklePrefixTrie.byteArrayAsHexString(key)+") = "+
				MerklePrefixTrie.byteArrayAsHexString(key));
		LOGGER.log(Level.FINE, "keyHash: "+MerklePrefixTrie.byteArrayAsBitString(leafNodeToAdd.getKeyHash()));
		Node newRoot = this.insertLeafNodeAndUpdate(this.root, leafNodeToAdd, 0);
		boolean updated = !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return updated;
	}
		
	/**
	 * Recursive helper function to insert a leaf into the trie. 
	 * @param node - current node in this tree
	 * @param nodeToAdd - LeafNode node we want to add 
	 * @param currentBitIndex - the current bit index. An int such that 
	 * location of node is nodeToAdd.getKeyHash()[:currentBitIndex]
	 * @return updated version of 'node' (i.e. location in tree)
	 */
	private Node insertLeafNodeAndUpdate(Node node, LeafNode nodeToAdd, int currentBitIndex) {
		if(node.isLeaf()) {
			// node is an empty leaf we can just replace it 
			if(node.isEmpty()) {
				return nodeToAdd;
			}
			// this node is already in the tree
			if(Arrays.equals(node.getKeyHash(), nodeToAdd.getKeyHash())) {
				return nodeToAdd;
			}
			// otherwise have to split
			LeafNode ln = (LeafNode) node;
			return this.split(ln, nodeToAdd, currentBitIndex);
		}
		boolean bit = MerklePrefixTrie.getBit(nodeToAdd.getKeyHash(), currentBitIndex);
		/*
		 * Encoding: if bit is 1 -> go right 
		 * 			 if bit is 0 -> go left
		 */
		if(bit) {
			Node result = this.insertLeafNodeAndUpdate(node.getRightChild(), nodeToAdd, currentBitIndex+1);
			return new InteriorNode(node.getLeftChild(), result);
		}
		Node result = this.insertLeafNodeAndUpdate(node.getLeftChild(), nodeToAdd, currentBitIndex+1);
		return new InteriorNode(result, node.getRightChild());
	}
	
	/**
	 * Recursive helper function to split two leaves which are currently mapped the the 
	 * same node
	 * @param a - the first leaf
	 * @param b - the second leaf
	 * @param currentBitIndex - a and b have the same prefix (collision) at least
	 *  up to currentBitIndex -1
	 * 	a.getKeyHash()[:currentBitIndex-1] == b.getKeyKash()[:currentBitIndex-1]
	 * @return
	 */
	private Node split(LeafNode a, LeafNode b, int currentBitIndex) {
		assert !Arrays.equals(a.getKeyHash(), b.getKeyHash());
		boolean bitA = MerklePrefixTrie.getBit(a.getKeyHash(), currentBitIndex);
		boolean bitB = MerklePrefixTrie.getBit(b.getKeyHash(), currentBitIndex);
		// still collision, split again
		if(bitA == bitB) {
			// recursively split 
			Node res;
			if(bitA) {
				// if bit is 1 add on the right 
				res = split(a, b, currentBitIndex+1);
				return new InteriorNode(new EmptyLeafNode(), res);
			}
			// if bit is 0 add on the left
			res = split(a, b, currentBitIndex+1);
			return new InteriorNode(res, new EmptyLeafNode());
		}
		// no collision
		if(bitA) {
			return new InteriorNode(b, a);
		}
		return new InteriorNode(a, b);
	}
	
	/**
	 * Get the value associated with a given key. Returns null if the key 
	 * is not in the MPT. 
	 * @param key
	 * @return
	 * @throws IncompleteMPTException - if parts of the MPT are missing such
	 * that the search cannot be completed this exception is 
	 * thrown. (E.g. if a MPT conatins a single path, and searching for this 
	 * key's prefix is not possible with just the path).
	 */
	public byte[] get(byte[] key) throws IncompleteMPTException {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "get H("+key+"): "+MerklePrefixTrie.byteArrayAsBitString(keyHash));
		return this.getKeyHelper(this.root, keyHash, 0);
	}
	
	/**
	 * Recursive helper function to search for the keyHash - a prefix
	 * in the trie. Returns when it finds a leaf. If the 
	 * key is not in the tree it will eventually hit an EmptyLeafNode or a 
	 * LeafNode with a different keyHash. In this case this function will return null.
	 * @param currentNode
	 * @param keyHash
	 * @param currentBitIndex
	 * @throws IncompleteMPTException - if we attempt to go down a path
	 * we do not have (i.e. currently represent by only a stub)
	 * @return
	 */
	private byte[] getKeyHelper(Node currentNode, byte[] keyHash, 
			int currentBitIndex) throws IncompleteMPTException {
		if(currentNode.isStub()) {
			throw new IncompleteMPTException("encountered a stub - cannot complete search");
		}
		if(currentNode.isLeaf()) {
			if(!currentNode.isEmpty()) {
				// if the current node is NonEmpty and matches the Key
				if(Arrays.equals(currentNode.getKeyHash(), keyHash)){
					return currentNode.getValue();
				}
			}
			// otherwise key not in the MPT - return null;
			return null;
		}
		/*
		 * Encoding: if bit is 1 -> go right 
		 * 			 if bit is 0 -> go left
		 */
		boolean bit = MerklePrefixTrie.getBit(keyHash, currentBitIndex);
		if(bit) {
			return this.getKeyHelper(currentNode.getRightChild(), keyHash, currentBitIndex+1);
		}
		return this.getKeyHelper(currentNode.getLeftChild(), keyHash, currentBitIndex+1);
	}
	
	/**
	 * Remove the key from the MPT, removing any (key, value) mapping if 
	 * it exists. 
	 * @param key - the key to remove
	 * @return true if the trie was modified (i.e. if the key was previously in the tree).
	 */
	public boolean deleteKey(byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "delete("+MerklePrefixTrie.byteArrayAsHexString(key)+") - Hash= "+MerklePrefixTrie.byteArrayAsBitString(keyHash));
		Node newRoot = this.deleteKeyHelper(this.root, keyHash, 0);
		boolean changed =  !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return changed;
	}
	
	private Node deleteKeyHelper(Node currentNode, 
			byte[] keyHash, int currentBitIndex) {
		if(currentNode.isLeaf()) {
			if(!currentNode.isEmpty()) {
				if(Arrays.equals(currentNode.getKeyHash(), keyHash)){
					return new EmptyLeafNode();
				}
			}
			// otherwise the key is not in the tree and nothing needs to be done
			return currentNode;
		}
		// we have to watch out to make sure that if this is the root node
		// that we return an InteriorNode and don't propagate up an empty node
		boolean isRoot = currentNode.equals(this.root);
		boolean bit = MerklePrefixTrie.getBit(keyHash, currentBitIndex);
		Node leftChild = currentNode.getLeftChild();
		Node rightChild = currentNode.getRightChild();
		if(bit) {
			// delete key from the right subtree
			Node newRightChild = this.deleteKeyHelper(rightChild, keyHash, currentBitIndex+1);
			// if left subtree is empty, and rightChild is leaf
			// we push the newRightChild back up the trie
			if(leftChild.isEmpty() && newRightChild.isLeaf() && !isRoot) {
				return newRightChild;
			}
			// if newRightChild is empty, and leftChild is a leaf
			// we push the leftChild back up the trie
			if(newRightChild.isEmpty() && leftChild.isLeaf() && !isRoot) {
				return leftChild;
			}
			// otherwise update the interior node
			return new InteriorNode(leftChild, newRightChild);
		}
		Node newLeftChild = this.deleteKeyHelper(leftChild, keyHash, currentBitIndex+1);
		if(rightChild.isEmpty() && newLeftChild.isLeaf() && !isRoot) {
			return newLeftChild;
		}
		if(newLeftChild.isEmpty() && rightChild.isLeaf() && !isRoot) {
			return rightChild;
		}
		return new InteriorNode(newLeftChild, rightChild);
	}
	
	/**
	 * Get the cryptographic commitment to this set of 
	 * (key, value) pairs
	 * @return
	 */
	public byte[] getCommitment() {
		return this.root.getHash();
	}
	
	/**
	 * Returns a new MPT that contains path that maps the key 
	 * to a possibly empty leaf in this trie. 
	 * The new MPT has the leaf entry as well as the hashes 
	 * on the path (in the form of stubs) needed to authenticate it
	 * @param - a key, arbitary bytes, may or may not be in the tree
	 * @return - a MerklePrefixTrie containing only the path 
	 * mapping the key to a leaf
	 */
	public MerklePrefixTrie copyPath(byte[] key) {
		int currBitIndex = 0;
		byte[] keyHash = CryptographicDigest.digest(key);
		Node res = this.pathBuilder(this.root, keyHash, currBitIndex);
		InteriorNode newRoot = (InteriorNode) res;
		return new MerklePrefixTrie(newRoot);
	}
	
	/**
	 * Recursive helper function for building a path to a leaf specified by a keyHash
	 * @param currNode the node in the trie we are copying from, located at the position
	 * keyHash[:currentIndex] 
	 * @param keyHash - the hash of the key (path in the MPT) to copy 
	 * @param currentIndex int s.t. path to currNode from root is keyHash[:currIndex]
	 * @return Node a Node with children that form the path to a leaf in this tree
	 */
	private Node pathBuilder(Node currNode, byte[] keyHash, int currIndex) {
		// base case
		if (currNode.isLeaf()) {
			if (currNode.isEmpty()) {
				// empty leaf (key not in MPT)
				return new EmptyLeafNode();
			} 
			// leaf node (key possibly in MPT, only if currNode.getKeyHash() == keyHash)
			return new LeafNode(currNode.getKey(), currNode.getValue());			
		}	
		//recursive step
		boolean bit = MerklePrefixTrie.getBit(keyHash, currIndex);
		if (bit) {
			//bit == 1 -> go right
			Node childOnPath = this.pathBuilder(currNode.getRightChild(), keyHash, currIndex + 1);
			// ignore path on left - stub
			Node leftStub = new Stub(currNode.getLeftChild().getHash());
			return new InteriorNode(leftStub, childOnPath);
		} 
		//bit == 0 -> go left
		Node childOnPath = this.pathBuilder(currNode.getLeftChild(), keyHash, currIndex + 1);
		// ignore path on right - stub
		Node rightStub = new Stub(currNode.getRightChild().getHash());
		return  new InteriorNode(childOnPath, rightStub);
	}
	
	/**
	 * Efficiently serializes this MPT. The serialization
	 * contains the minimal set of information required to reconstruct 
	 * and authenticate this MPT when deserialized on the client.
	 * @return
	 */
	public byte[] serialize() {
		MptSerialization.Node rootSerialization = this.root.serialize();
		MptSerialization.MerklePrefixTrieProof.Builder builder = 
				MptSerialization.MerklePrefixTrieProof.newBuilder();
		builder.setRoot(rootSerialization);
		return builder.build().toByteArray();
	}
	
	/**
	 * Parses a serialized MPT from raw bytes. Throws an 
	 * InvalidMPTSerialization if the tree cannot be deserialized properly
	 * @param asbytes
	 * @return
	 * @throws InvalidMPTSerialization
	 */
	public static MerklePrefixTrie deserialize(byte[] asbytes) throws 
	InvalidMPTSerializationException	{
		MptSerialization.MerklePrefixTrieProof mptProof;
		try {
			mptProof = MptSerialization.MerklePrefixTrieProof.parseFrom(asbytes);
		}catch(InvalidProtocolBufferException e) {
			throw new InvalidMPTSerializationException(e.getMessage());
		}
		if(!mptProof.hasRoot()) {
			throw new InvalidMPTSerializationException("no root included");
		}
		Node root = MerklePrefixTrie.parseNode(mptProof.getRoot());
		if(! ( root instanceof InteriorNode )) {
			throw new InvalidMPTSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MerklePrefixTrie(rootInt);
	}
	
	/**
	 * Helper function with recursively parses the individual serialized 
	 * nodes into MerklePrefixTrie nodes according to the mpt.proto format.
	 * Throws InvalidMPTSerialization if a node is not correctly formatted
	 * and cannot be parsed
	 * @param nodeSerialization
	 * @return
	 * @throws InvalidMPTSerialization
	 */
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws 
	InvalidMPTSerializationException {
		switch(nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE :
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			Node left, right;
			/*
			 * If an interior node child is not present, we assume it is an empty child
			 */
			if(!in.hasLeft()) {
				left = new EmptyLeafNode();
			}else {
				left = MerklePrefixTrie.parseNode(in.getLeft());
			}
			if(!in.hasRight()) {
				right = new EmptyLeafNode();
			}else {
				right = MerklePrefixTrie.parseNode(in.getRight());
			}
			return new InteriorNode(left, right);
		case STUB :
			MptSerialization.Stub stub = nodeSerialization.getStub();
			if(stub.getHash().isEmpty()) {
				throw new InvalidMPTSerializationException("stub doesn't have a hash");
			}
			return new Stub(stub.getHash().toByteArray());
		case LEAF : 
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if(leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidMPTSerializationException("leaf doesn't have required keyhash and value");
			}
			return new LeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case NODE_NOT_SET : 
			throw new InvalidMPTSerializationException("no node included - fatal error");
		}
		return null;
	}
	
	/**
	 * Returns the height of the tree. Height is defined 
	 * as the maximum possible distance from the leaf to the root node
	 * (TODO: I'm not sure this should be a public method - only really 
	 *  useful for benchmarking purposes)
	 * @return
	 * @throws IncompleteMPTException
	 */
	public int getMaxHeight() throws IncompleteMPTException{
		return this.getHeightRecursive(this.root);
	}
		
	private int getHeightRecursive(Node currentLocation) throws IncompleteMPTException {
		// if we encounter a stub we do not have the entire tree
		// so we cannot determine the height
		if (currentLocation.isStub()) {
			throw new IncompleteMPTException("stub encountered - cannot determine height of tree");
		}
		// each leaf is at height zero
		if (currentLocation.isLeaf()) {
			return 0;
		}
		// otherwise we are at an interior node - height is maximum of 
		// the height of the children plus 1
		return Math.max(
				this.getHeightRecursive(currentLocation.getLeftChild()), 
				this.getHeightRecursive(currentLocation.getRightChild())) + 1;
	}
	
	
	@Override
	public String toString() {
		return this.toStringHelper("+", this.root);
	}
	
	private String toStringHelper(String prefix, Node node) {
		String result = prefix+" "+node.toString();
		if(!node.isLeaf() && !node.isStub()) {
			String left = this.toStringHelper(prefix+"0", node.getLeftChild());
			String right = this.toStringHelper(prefix+"1", node.getRightChild());
			result = result+"\n"+left+"\n"+right;
		}
		return result;
	}
	
	/**
	 * Two MPTs are equal if they are STRUCTURALLY IDENTICAL which means
	 * two trees are equal if the contain identical nodes arranged in the
	 * same way.
	 */
	@Override
	public boolean equals(Object other) {
		if(other instanceof MerklePrefixTrie) {
			MerklePrefixTrie othermpt = (MerklePrefixTrie) other;
			return this.root.equals(othermpt.root);
		}
		return false;
	}
	
	/**
	 * TODO: these helper functions should be moved to a dedicated Util class!
	 * to clean things up and keep the code files small
	 */
	
	
	/**
	 * Get the bit at index in a byte array. 
	 * byte array:   byte[0]|| byte[1] || byte[2]  || byte[3]
	 * index		 [0...7]  [8...15]   [16...23]    [24...31]
	 * @param bytes array of bytes representing a single value (byte[0]||byte[1]||..)
	 * @param index the index of the bit
	 * @return true if the bit is 1 and false if the bit is 0
	 */
	public static boolean getBit(final byte[] bytes, int index) {
		int byteIndex = Math.floorDiv(index, 8); 
		//int bitIndex = index % 8;
		int bitIndex = (7 - index) % 8;
		if (bitIndex < 0) {
			bitIndex += 8;
		}
		byte b = bytes[byteIndex];
		return MerklePrefixTrie.getBit(b, bitIndex);
	}
	
	/**
	 * Get the index'th bit in a byte 
	 * @param b a byte
	 * @param index index of the bit to get in [0, 7] (0-indexed)
	 * @return
	 */
	public static boolean getBit(final byte b, int index) {
		switch(index) {
		case 0:
			return (b & 1) != 0;
		case 1:
			return (b & 2) != 0;
		case 2:
			return (b & 4) != 0;
		case 3:
			return (b & 8) != 0;
		case 4:
			return (b & 0x10) != 0;
		case 5:
			return (b & 0x20) != 0;
		case 6:
			return (b & 0x40) != 0;
		case 7:
			return (b & 0x80) != 0;
		}
		throw new RuntimeException("Only 8 bits in a byte - bit index must between 0 and 7");
	}
	
	/**
	 * Return an array of bytes as string of bits
	 * @param bytes
	 * @return
	 */
	public static String byteArrayAsBitString(final byte[] bytes) {
		String bitString = "";
		for(byte b: bytes) {
			//for(int bitIndex = 0; bitIndex < 8; bitIndex++) {
			for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
				if(MerklePrefixTrie.getBit(b, bitIndex)) {
					bitString += "1";
				}else {
					bitString += "0";
				}
			}
		}
		return bitString;
	}
	
	/**
	 * Print a byte array as a human readable hex string
	 * @param raw
	 * @return
	 */
	public static String byteArrayAsHexString(final byte[] raw) {
	    final StringBuilder hex = new StringBuilder(2 * raw.length);
	    for (final byte b : raw) {
	        hex.append(Integer.toHexString(b));
	    }
	    return hex.toString();
	}
	
}
