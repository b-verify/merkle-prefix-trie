package mpt;

import java.security.NoSuchAlgorithmException;

public class MerklePrefixTrie {
	
	private Node root;
	
	public MerklePrefixTrie() {
		root = new EmptyLeafNode();
	}
	
	public boolean set(byte[] key, byte[] value){
		int currentBitIndex = 0;
		Node currentNode = root;
		
		System.out.println("Adding key: "+MerklePrefixTrie.byteArrayAsBitString(key));
		
		// for debugging 
		String prefix = "";

		
		return false;
	}
	
	private Node insertLeafNodeAndUpdate(Node node, byte[] key, byte[] value, int currentBitIndex, String prefix) {
		try {
			if(node.isLeaf()) {
				// if leaf is empty we just replace it!
				if(node.isEmpty()) {
					return new LeafNode(value);
				}else {
					// otherwies we need to split!
				}
			}
			return null;
		}catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public byte[] get(byte[] key) {
		int currentBitIndex = 0;
		Node currentNode = root;
		
		System.out.println("Searching key: "+MerklePrefixTrie.byteArrayAsBitString(key));
		
		// for debugging 
		String prefix = "";
		
		while(true) {
			System.out.println("Searching prefix: "+prefix);
			// if it is a leaf the search is over
			// could be the result for that key 
			// or an empty leaf
			if(currentNode.isLeaf()) {
				System.out.println("value found: "+currentNode.getValue());
				return currentNode.getValue();
			}
			
			// otherwise continue the search according to the key
			boolean bit = MerklePrefixTrie.getBit(key, currentBitIndex);
			if(bit) {
				prefix += "1";
				currentNode = currentNode.getLeftChild();
			}else {
				prefix += "0";
				currentNode = currentNode.getRightChild();
			}
		}

	}
	
	public byte[] getCommitment() {
		return root.getHash();
	}
	
	@Override
	public String toString() {
		return this.toStringHelper("", this.root);
	}
	
	private String toStringHelper(String prefix, Node node) {
		String result = prefix+" "+node.toString();
		if(node.hasLeftChild()) {
			String left = this.toStringHelper(prefix+"0", node.getLeftChild());
			result = result+"\n"+left;
		}
		if(node.hasRightChild()) {
			String right = this.toStringHelper(prefix+"1", node.getRightChild());
			result = result+"\n"+right;
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
	static boolean getBit(final byte[] bytes, int index) {
		int byteIndex = Math.floorDiv(index, 8); 
		int bitIndex = index % 8;
		byte b = bytes[byteIndex];
		MerklePrefixTrie.getBit(b, bitIndex);
		return false;
	}
	
	
	static boolean getBit(final byte b, int index) {
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
	
	static String byteArrayAsBitString(final byte[] bytes) {
		String bitString = "";
		for(byte b: bytes) {
			for(int bitIndex = 0; bitIndex < 8; bitIndex++) {
				bitString = bitString+MerklePrefixTrie.getBit(b, bitIndex);
			}
		}
		return bitString;
	}
	
}
