package mpt.core;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * MUTABLE
 * 
 * Represents a leaf node in a Merkle Prefix Trie (MPT) set.
 * Set leaf nodes store only a value (unlike in a dictionary
 * where the leaf nodes store key and values). Unlike in 
 * dictionary leaf nodes, the value of a set leaf node 
 * cannot be updated.
 * 
 * @author henryaspegren
 *
 */
public class SetLeafNode implements Node{
	
	private byte[] value;
	private byte[] valueHash;
	
	private boolean changed;
	
	public SetLeafNode(byte[] value) {
		this.value = value.clone();
		this.valueHash = CryptographicDigest.hash(value);
		this.changed = true;
	}

	@Override
	public byte[] getValue() {
		return this.value;
	}

	@Override
	public void setValue(byte[] value) {
		throw new RuntimeException("cannot set the value in a SetLeafNode");
	}

	@Override
	public byte[] getHash() {
		return this.valueHash.clone();
	}

	@Override
	public byte[] getKey() {
		return this.value.clone();
	}

	@Override
	public byte[] getKeyHash() {
		return this.valueHash.clone();
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
	public boolean isStub() {
		return false;
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

	@Override
	public serialization.MptSerialization.Node serialize() {
		MptSerialization.Node node = MptSerialization.Node
				.newBuilder()
				.setLeaf(MptSerialization.Leaf.newBuilder()
						.setValue(ByteString.copyFrom(this.value))
						.build())
				.build();
		return node;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof SetLeafNode) {
			SetLeafNode othersln = (SetLeafNode) other;
			return Arrays.equals(othersln.value, this.value);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return new String("<SetLeaf V: "+ Utils.byteArrayAsHexString(this.value) +
				" Hash: " + Utils.byteArrayAsHexString(this.getHash())
				+">");	
	}
	
}
