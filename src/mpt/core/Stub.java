package mpt.core;

import java.util.Arrays;

import com.google.protobuf.ByteString;

/**
 * IMMUTABLE
 * 
 * A stub represents an omitted path in a MPT. Stubs only store a 
 * hash commitment to that subtree. These can be used to construct proofs and
 * can be swapped out for actual subtrees which match the hash
 * @author henryaspegren
 *
 */
public class Stub implements Node {
	
	private final byte[] hash;
	
	public Stub(byte[] hash) {
		this.hash = hash.clone();
	}
	
	@Override
	public byte[] getHash() {
		return this.hash.clone();
	}
	
	public static Stub deserialize(byte[] raw) {
		return null;
	}
	
	@Override 
	public serialization.generated.MptSerialization.Node serialize(){
		serialization.generated.MptSerialization.Node node =
				serialization.generated.MptSerialization.Node
						.newBuilder()
						.setStub(
								serialization.generated.MptSerialization.Stub.newBuilder()
								.setHash(ByteString.copyFrom(this.hash))
								.build())
						.build();
		return node;
	}

	@Override
	public byte[] getKey() {
		return null;
	}

	@Override
	public byte[] getValue() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isStub() {
		return true;
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
	public String toString() {
		String hex = Utils.byteArrayAsHexString(this.hash);
		return "<Stub Hash: " + hex + ">";
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Stub) {
			return Arrays.equals(this.hash, ((Stub) other).hash);
		}
		return false;
	}

	@Override
	public void setValue(byte[] value) {
		throw new RuntimeException("cannot set value of a stub");
	}

	@Override
	public void setLeftChild(Node leftChild) {
		throw new RuntimeException("cannot set children of a stub");
	}

	@Override
	public void setRightChild(Node rightChild) {
		throw new RuntimeException("cannot set children of a stub");		
	}

	@Override
	public boolean changed() {
		return false;
	}

	@Override
	public void markChangedAll() {
		
	}

	@Override
	public void markUnchangedAll() {
		
	}
}
