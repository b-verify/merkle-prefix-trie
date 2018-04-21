package mpt;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

/**
 * This class tracks the changes to a Merkle Prefix Trie (a delta).
 * This delta contains ONLY the changed nodes. Nodes that have 
 * not been changed are represented as STUBS. 
 * 
 * This information can be used to construct update proofs for
 * clients 
 * 
 * @author henryaspegren
 *
 */
public class MerklePrefixTrieDelta implements AuthenticatedDictionaryChanges {
	
	protected InteriorNode root;
	
	/**
	 * Construct a MerklePrefixTrieDelta from a full MPT. It only copies
	 * the changes the from the MPT (where changes are defined as any nodes
	 * altered by inserts or deletes since the last call to mpt.reset())
	 * @param mpt - The MPT to copy changes from
	 */
	public MerklePrefixTrieDelta(MerklePrefixTrieFull mpt) {
		InteriorNode copiedRootOnlyChanges = (InteriorNode) MerklePrefixTrieDelta.copyChangesOnlyHelper(mpt.root);
		this.root = copiedRootOnlyChanges;
	}

	private static Node copyChangesOnlyHelper(final Node currentNode) {
		if(!currentNode.changed()) {
			return new Stub(currentNode.getHash());
		}
		if (currentNode.isLeaf()) {
			if (currentNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			if (currentNode.changed()) {
				return new LeafNode(currentNode.getKey(), currentNode.getValue());
			}
			return new Stub(currentNode.getHash());
		}
		Node leftChild = MerklePrefixTrieDelta.copyChangesOnlyHelper(currentNode.getLeftChild());
		Node rightChild = MerklePrefixTrieDelta.copyChangesOnlyHelper(currentNode.getRightChild());
		return new InteriorNode(leftChild, rightChild);
	}

	public MptSerialization.MerklePrefixTrie getUpdates(final byte[] key) {
		List<byte[]> keys = new ArrayList<byte[]>();
		keys.add(key);
		return this.getUpdates(keys);
	}
	
	public MptSerialization.MerklePrefixTrie getUpdates(final List<byte[]> keys) {
		List<byte[]> keyHashes = new ArrayList<byte[]>();
		for(byte[] key : keys) {
			keyHashes.add(CryptographicDigest.digest(key));
		}
		MptSerialization.Node root = MerklePrefixTrieDelta.getUpdatesHelper(keyHashes, -1, this.root);
		MptSerialization.MerklePrefixTrie tree = MptSerialization.MerklePrefixTrie.newBuilder()
				.setRoot(root)
				.build();
		return tree;
	}
	
	private static MptSerialization.Node getUpdatesHelper(final List<byte[]> matchingKeyHashes, 
			final int currentBitIndex, final Node currentNode){
		// case: stub - this location has not changed 
		// 				--> avoid re-transmitting it by caching it on the client 
		if(currentNode.isStub()) {
			return null;
		}
		// case: non-stub - this location has changed 
		// subcase: no matching keys - value is not needed
		if(matchingKeyHashes.size() == 0) {
			// if empty, just send empty node
			if(currentNode.isEmpty()) {
				return MptSerialization.Node.newBuilder()
						.setEmptyleaf(MptSerialization.EmptyLeaf.newBuilder())
						.build();
			}
			// if non-empty send stub
			return MptSerialization.Node.newBuilder()
					.setStub(MptSerialization.Stub.newBuilder()
							.setHash(ByteString.copyFrom(currentNode.getHash())))
					.build();
		}
		// subcase: have a matching key and at end of path 
		if(currentNode.isLeaf()) {
			// if empty, just send empty node
			if(currentNode.isEmpty()) {
				return MptSerialization.Node.newBuilder().setEmptyleaf(
						MptSerialization.EmptyLeaf.newBuilder()).build();
			}
			// if non-empty send entire leaf (since value needed)
			return MptSerialization.Node.newBuilder().setLeaf(
						MptSerialization.Leaf.newBuilder()
							.setKey(ByteString.copyFrom(currentNode.getKey()))
							.setValue(ByteString.copyFrom(currentNode.getValue())))
					.build();
		}
		// subcase: have a matching leaf and at intermediate node
		
		// divide up keys into those that match the right prefix (...1)
		// and those that match the left prefix (...0)
		List<byte[]> matchRight = new ArrayList<byte[]>();
		List<byte[]> matchLeft = new ArrayList<byte[]>();
		for(byte[] keyHash : matchingKeyHashes) {
			final boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
			if(bit) {
				matchRight.add(keyHash);
			}else {
				matchLeft.add(keyHash);
			}
		}
		MptSerialization.Node left = MerklePrefixTrieDelta.getUpdatesHelper(matchLeft, currentBitIndex+1, 
				currentNode.getLeftChild());
		MptSerialization.Node right = MerklePrefixTrieDelta.getUpdatesHelper(matchRight, currentBitIndex+1, 
				currentNode.getRightChild());
		
		// create an interior node to return
		MptSerialization.InteriorNode.Builder interiorBuilder = MptSerialization.InteriorNode.newBuilder();
		// omit unchanged stubs since they are cached on the client 
		if(right != null) {
			interiorBuilder.setRight(right);
		}
		if(left != null) {
			interiorBuilder.setLeft(left);
		}
		return MptSerialization.Node.newBuilder().setInteriorNode(interiorBuilder).build();
	}
		
	@Override
	public String toString() {
		return "<MerklePrefixTrieDelta \n"+MerklePrefixTrieFull.toStringHelper("+", this.root)+"\n>";
	}

}
