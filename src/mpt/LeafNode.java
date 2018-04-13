package mpt;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

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
		
	public MptSerialization.Node serialize(){
		MptSerialization.Node node = MptSerialization.Node
				.newBuilder()
				.setLeaf(MptSerialization.Leaf.newBuilder()
						.setKey(ByteString.copyFrom(this.key))
						.setValue(ByteString.copyFrom(this.value))
						.build())
				.build();
		return node;
	}
	
	@Override
	public byte[] getValue() {
		return this.value.clone();
	}
		
	@Override
	public byte[] getHash() {
		return this.valueHash.clone();
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
		return new String("<Leaf K: "+ Utils.byteArrayAsHexString(this.key) +
				" V: "+ Utils.byteArrayAsHexString(this.value) + 
				" Hash: " + Utils.byteArrayAsHexString(this.getHash())
				+">");
	}

	@Override
	public byte[] getKey() {
		return this.key.clone();
	}

	@Override
	public byte[] getKeyHash() {
		return this.keyHash.clone();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof LeafNode) {
			LeafNode ln = (LeafNode) arg0;
			// practically speaking it would be sufficient to just check the hashes
			// since we are using collision resistant hash functions 
			return Arrays.equals(this.key, ln.key) && Arrays.equals(this.value, ln.value) &&
					Arrays.equals(this.keyHash, ln.keyHash) && Arrays.equals(this.valueHash, ln.valueHash);
		}
		return false;
	}
	
	@Override
	public boolean isStub() {
		return false;
	}
	
}
