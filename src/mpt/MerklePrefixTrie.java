package mpt;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import crpyto.CryptographicDigest;

/**
 * A  Merkle Prefix Trie (MPT) 
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
		root = new InteriorNode(new EmptyLeafNode(), new EmptyLeafNode());
	}
	
	/**
	 * Add a (key, value) mapping to the MPT.
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return
	 */
	public boolean set(byte[] key, byte[] value){
		LeafNode leafNodeToAdd = new LeafNode(key, value);
		LOGGER.log(Level.FINE, "Adding LeafNode: "+leafNodeToAdd);
		LOGGER.log(Level.FINE, "keyHash: "+MerklePrefixTrie.byteArrayAsBitString(leafNodeToAdd.getKeyHash()));
		
		Node newRoot = this.insertLeafNodeAndUpdate(this.root, leafNodeToAdd, 0, "+");
		boolean updated = !newRoot.equals(this.root);
		this.root = newRoot;
		return updated;
	}
	
	/**
	 * Recursive helper function to insert a leaf into the trie. 
	 * @param node - location in the tree
	 * @param nodeToAdd - LeafNode node we want to add 
	 * @param currentBitIndex - the bit index in the prefix trie
	 * @param prefix - the current bit prefix 
	 * @return
	 */
	private Node insertLeafNodeAndUpdate(Node node, LeafNode nodeToAdd, int currentBitIndex, String prefix) {
		LOGGER.log(Level.FINE, "insert and update "+prefix+" at node: "+node.toString());
		if(node.isLeaf()) {
			// already in the tree!
			if(node.equals(nodeToAdd)) {
				return node;
			}
			// node is an empty leaf we can just replace it 
			if(node.isEmpty()) {
				return nodeToAdd;
			}
			LeafNode ln = (LeafNode) node;
			// otherwise have to split
			return this.split(ln, nodeToAdd, currentBitIndex, prefix);
		}
		boolean bit = MerklePrefixTrie.getBit(nodeToAdd.getKeyHash(), currentBitIndex);
		/*
		 * Encoding: if bit is 1 -> go right 
		 * 			 if bit is 0 -> go left
		 */
		if(bit) {
			Node result = this.insertLeafNodeAndUpdate(node.getRightChild(), nodeToAdd, currentBitIndex+1, prefix+"1");
			return new InteriorNode(node.getLeftChild(), result);
		}
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
		LOGGER.log(Level.FINE, "Searching H("+key+"): "+MerklePrefixTrie.byteArrayAsBitString(keyHash));
		return this.getKeyHelper(this.root, keyHash, 0, "+");
	}
	
	private byte[] getKeyHelper(final Node currentNode, byte[] keyHash, 
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
	 * Get the cryptographic commitment to this set of 
	 * (key, value) pairs
	 * @return
	 */
	public byte[] getCommitment() {
		return root.getHash();
	}
	
	@Override
	public String toString() {
		return this.toStringHelper("+", this.root);
	}
	
	private String toStringHelper(String prefix, Node node) {
		String result = prefix+" "+node.toString();
		if(!node.isLeaf()) {
			String left = this.toStringHelper(prefix+"0", node.getLeftChild());
			String right = this.toStringHelper(prefix+"1", node.getRightChild());
			result = result+"\n"+left+"\n"+right;
		}
		return result;
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
		int bitIndex = index % 8;
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
	 * Print an array of bytes as string of bits
	 * @param bytes
	 * @return
	 */
	public static String byteArrayAsBitString(final byte[] bytes) {
		String bitString = "";
		for(byte b: bytes) {
			for(int bitIndex = 0; bitIndex < 8; bitIndex++) {
				if(MerklePrefixTrie.getBit(b, bitIndex)) {
					bitString += "1";
				}else {
					bitString += "0";
				}
			}
		}
		return bitString;
	}
	
}
