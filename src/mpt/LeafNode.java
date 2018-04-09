package mpt;

import java.util.Arrays;

import crpyto.CryptographicDigest;

/**
 * IMMUTABLE
 * 
 * Represents a leaf node in a Merkle Prefix Trie (MPT). Leaf nodes
 * store a key and a value, which can be arbitrary bytes.
 * @author henryaspegren
 *
 */
public class LeafNode implements Node {
	
	// the key can be arbitrary bytes
	// (e.g a pubkey, a set of pubkeys, a string)
	private final byte[] key;
	private final byte[] keyHash;
	
	// the value stored in this leaf
	// this can be arbitrary bytes 
	// (e.g. a commitment, a string)
	private final byte[] value;
	private final byte[] valueHash;
		
	public LeafNode(byte[] key, byte[] value){
		this.key = key;
		this.keyHash = CryptographicDigest.digest(key);
		this.value = value;
		this.valueHash = CryptographicDigest.digest(value);
	}
	
	@Override
	public byte[] getValue() {
		return this.value;
	}
		
	@Override
	public byte[] getHash() {
		return this.valueHash;
	}
	

	@Override
	public Node getLeftChild() {
		return null;
	}

	@Override
	public Node getRightChild() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
		
	@Override
	public String toString() {
		return new String("<Leaf K: "+this.key+" V: "+this.value+">");
	}

	@Override
	public byte[] getKey() {
		return this.key;
	}

	@Override
	public byte[] getKeyHash() {
		return this.keyHash;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof LeafNode) {
			LeafNode ln = (LeafNode) arg0;
			return Arrays.equals(this.keyHash, ln.keyHash) && Arrays.equals(this.valueHash, ln.valueHash);
		}
		return false;
	}
	
}
