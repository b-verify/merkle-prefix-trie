package mpt;

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
		this.hash = hash;
	}

	@Override
	public byte[] getHash() {
		return this.hash.clone();
	}
	
	public static Stub deserialize(byte[] raw) {
		return null;
	}
	
	@Override 
	public serialization.MptSerialization.Node serialize(){
		serialization.MptSerialization.Node node =
				serialization.MptSerialization.Node
						.newBuilder()
						.setStub(
								serialization.MptSerialization.Stub.newBuilder()
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
	public byte[] getKeyHash() {
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
		String hex = Utils.byteArrayAsHexString(this.getHash());
		return "<Stub Hash: " + hex + ">";
	}
}
