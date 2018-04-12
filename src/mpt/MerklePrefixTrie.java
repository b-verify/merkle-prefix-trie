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
 * @author henryaspegren
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
		
		Node newRoot = this.insertLeafNodeAndUpdate(this.root, leafNodeToAdd, 0, "+");
		boolean updated = !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return updated;
	}
		
	/**
	 * Recursive helper function to insert a leaf into the trie. 
	 * @param node - location in the tree
	 * @param nodeToAdd - LeafNode node we want to add 
	 * @param currentBitIndex - the bit index in the prefix trie
	 * @param prefix - the current bit prefix 
	 * @return updated version of 'node' (i.e. location in tree)
	 */
	private Node insertLeafNodeAndUpdate(Node node, LeafNode nodeToAdd, int currentBitIndex, String prefix) {
		LOGGER.log(Level.FINE, "insert and update "+prefix+" at node: "+node.toString());
		System.out.println("inserting");
		System.out.println("Node: " + node);
		System.out.println("node to add: " + nodeToAdd);
		System.out.println("curr bit index: " + currentBitIndex);
		System.out.println("prefix: " + prefix);
		
		if(node.isLeaf()) {
			// node is an empty leaf we can just replace it 
			if(node.isEmpty()) {
				return nodeToAdd;
			}
			// this node has the same key - already in the tree
			if(Arrays.equals(node.getKeyHash(), nodeToAdd.getKeyHash())) {
				return nodeToAdd;
			}
			// otherwise have to split
			LeafNode ln = (LeafNode) node;
			System.out.println("SPLITTING");
			return this.split(ln, nodeToAdd, currentBitIndex, prefix);
		}
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(nodeToAdd.getKeyHash()));
		boolean bit = MerklePrefixTrie.getBit(nodeToAdd.getKeyHash(), currentBitIndex);
		/*
		 * Encoding: if bit is 1 -> go right 
		 * 			 if bit is 0 -> go left
		 */
		if(bit) {
			System.out.println("bit = 1");
			Node result = this.insertLeafNodeAndUpdate(node.getRightChild(), nodeToAdd, currentBitIndex+1, prefix+"1");
			return new InteriorNode(node.getLeftChild(), result);
		}
		System.out.println("bit = 0");
		Node result = this.insertLeafNodeAndUpdate(node.getLeftChild(), nodeToAdd, currentBitIndex+1, prefix+"0");
		return new InteriorNode(result, node.getRightChild());
	}
	
	/**
	 * Recursive helper function to split two leaves which are currently mapped the the 
	 * same node
	 * @param a - the first leaf
	 * @param b - the second leaf
	 * @param currentBitIndex - keyHash of a and keyHash of b collide up to currentBitIndex-1
	 * @param prefix - the current bit prefix of the collision
	 * @return
	 */
	private Node split(LeafNode a, LeafNode b, int currentBitIndex, String prefix) {
		assert !Arrays.equals(a.getKeyHash(), b.getKeyHash());
		boolean bitA = MerklePrefixTrie.getBit(a.getKeyHash(), currentBitIndex);
		boolean bitB = MerklePrefixTrie.getBit(b.getKeyHash(), currentBitIndex);
		LOGGER.log(Level.FINE, "Splitting "+prefix+ " at "+currentBitIndex+" | a: "+bitA+" b: "+bitB);

		// still collision, split again
		if(bitA == bitB) {
			// recursively split 
			Node res;
			if(bitA) {
				// if bit is 1 add on the right 
				res = split(a, b, currentBitIndex+1, prefix+"1");
				return new InteriorNode(new EmptyLeafNode(), res);
			}
			// if bit is 0 add on the left
			res = split(a, b, currentBitIndex+1, prefix+"0");
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
	 * is not in the MPT
	 * @param key
	 * @return
	 */
	public byte[] get(byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "get H("+key+"): "+MerklePrefixTrie.byteArrayAsBitString(keyHash));
		return this.getKeyHelper(this.root, keyHash, 0, "+");
	}
	
	/**
	 * Recursive helper function to search for the keyHash. Returns when it finds a leaf. If the 
	 * key is not in the tree it will eventually hit an EmptyLeafNode or a 
	 * LeafNode with a different keyHash. In this case this function will return null.
	 * @param currentNode
	 * @param keyHash
	 * @param currentBitIndex
	 * @param prefix
	 * @return
	 */
	private byte[] getKeyHelper(Node currentNode, byte[] keyHash, 
			int currentBitIndex, String prefix) {
		LOGGER.log(Level.FINE, "Searching prefix "+prefix+" at node: "+currentNode);
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
			return this.getKeyHelper(currentNode.getRightChild(), keyHash, currentBitIndex+1, prefix+"1");
		}
		return this.getKeyHelper(currentNode.getLeftChild(), keyHash, currentBitIndex+1, prefix+"0");
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
		Node newRoot = this.deleteKeyHelper(this.root, keyHash, 0, "+");
		boolean changed =  !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return changed;
	}
	
	private Node deleteKeyHelper(Node currentNode, 
			byte[] keyHash, int currentBitIndex, String prefix) {
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
			Node newRightChild = this.deleteKeyHelper(rightChild, keyHash, currentBitIndex+1, prefix+"1");
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
		Node newLeftChild = this.deleteKeyHelper(leftChild, keyHash, currentBitIndex+1, prefix+"0");
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
	 * Returns a MerklePrefixTrie that contains path to a node specified
	 * by byte[] path to it
	 * @param key path to a non-root node (so key cannot be empty)
	 * @return
	 */
	public MerklePrefixTrie copyPath(byte[] key) {
		
		MerklePrefixTrie copy = new MerklePrefixTrie();
		
		//check whether key is nonempty
		if (key.length == 0) {
			//return empty tree for empty key
			return copy;
		}
		
		
		int currBitIndex = 0;
		Node newRoot = this.pathBuilder(this.root, key, currBitIndex);
		
		
		copy.forceAddRoot(newRoot);
		
		
		
		return copy;
		
	}
	
	/**
	 * Recursive helper function for building a path to a leaf specified by a certain key
	 * @param currNode the node in the trie we are trying to copy from, located at the position
	 * key[:currentIndex]
	 * @param key - binary path to a leaf
	 * @param currentIndex int s.t. path to currNode from root is key[:currIndex]
	 * @return Node a Node with children that form the path to a leaf in this tree
	 */
	private Node pathBuilder(Node currNode, byte[] key, int currIndex) {
		System.out.println("in pathBuilder");
		System.out.println("currNode: " + currNode);
		System.out.println("currIndex: " + currIndex);
		
		byte[] keyHash = CryptographicDigest.digest(key);
		//if (currIndex >= key.length) {
		if (currNode.isLeaf()) {
			//base case
			
			//check whether destination node should be stub node or actual leaf node
			Node end;
			if (currNode.isEmpty()) {
				//check for empty leaves
				end = new EmptyLeafNode();
			} else {
				
				end = new LeafNode(currNode.getKey(), currNode.getValue());
			}
			
			return end;
			
		} else {
			
			//recursive step
			Node childOnPath;
			Node path;
			boolean bit = MerklePrefixTrie.getBit(keyHash, currIndex);
			if (bit) {
				//bit == 1 -> go right
				childOnPath = this.pathBuilder(currNode.getRightChild(), keyHash, currIndex + 1);
				Node leftStub = new Stub(currNode.getLeftChild().getHash());
				path = new InteriorNode(leftStub, childOnPath);
			} else {
				//bit == 0 -> go left
				childOnPath = this.pathBuilder(currNode.getLeftChild(), keyHash, currIndex + 1);
				Node rightStub = new Stub(currNode.getRightChild().getHash());
				path = new InteriorNode(childOnPath, rightStub);
			}
			
			return path;
		}
		
		
	}
	
	private void forceAddRoot(Node root) {
		this.root = root;
		
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
			//System.out.println("curr node: "+node);
			//System.out.println("is leaf: " + node.isLeaf());
			//System.out.println("is stub: " + node.isStub());
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
