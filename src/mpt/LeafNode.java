package mpt;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * MUTABLE
 * 
 * Represents a leaf node in a Merkle Prefix Trie (MPT). Leaf nodes
 * store a key and a value, which can be arbitrary bytes. The value of
 * a leaf can be updated. The hash is calculated lazily.
 *  
 * @author henryaspegren
 *
 */
public class LeafNode implements Node {
		
	// the key can be arbitrary bytes
	// (e.g a pubkey, a set of pubkeys, a string)
	private final byte[] key;
	// H(key)
	private final byte[] keyHash;
	
	// the value stored in this leaf
	// this can be arbitrary bytes 
	// (e.g. a commitment, a string)
	private byte[] value;
	private boolean changed;
	
	// the commitment is a witness to BOTH 
	// the key and value: H(H(key)||H(value))
	private byte[] commitmentHash;
	private boolean recalculateHash;
		
	public LeafNode(byte[] key, byte[] value){
		this.key = key.clone();
		this.keyHash = CryptographicDigest.digest(key);
		this.value = value.clone();
		this.changed = true;
		this.recalculateHash = true;
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
		if(this.recalculateHash) {
			// witness
			this.commitmentHash = CryptographicDigest.witnessKeyAndValue(this.key, this.value);
			this.recalculateHash = false;
		}
		return this.commitmentHash.clone();
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
			return Arrays.equals(this.key, ln.key) && Arrays.equals(this.value, ln.value);
		}
		return false;
	}
	
	@Override
	public boolean isStub() {
		return false;
	}

	@Override
	public void setValue(byte[] value) {
		if(!Arrays.equals(this.value, value)) {
			// update the value and the witness
			this.value = value.clone();
			this.changed = true;
			this.recalculateHash = true;
		}
	}

	@Override
	public void setLeftChild(Node leftChild) {
		throw new RuntimeException("cannot set child of a leaf node");
	}

	@Override
	public void setRightChild(Node rightChild) {
		throw new RuntimeException("cannot set child of a leaf node");		
	}

	@Override
	public boolean changed() {
		return this.changed;
	}


	@Override
	public void markChangedAll() {
		this.changed = true;
	}

	@Override
	public void markUnchangedAll() {
		this.changed = false;
	}
	
}
