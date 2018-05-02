package mpt.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import mpt.core.DictionaryLeafNode;
import mpt.core.EmptyLeafNode;
import mpt.core.InteriorNode;
import mpt.core.Node;
import mpt.core.Stub;
import mpt.core.Utils;
import serialization.generated.MptSerialization;

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
public class MPTDictionaryDelta implements AuthenticatedDictionaryChanges {
	
	protected InteriorNode root;
	
	/**
	 * Construct a MerklePrefixTrieDelta from a full MPT. It only copies
	 * the changes the from the MPT (where changes are defined as any nodes
	 * altered by inserts or deletes since the last call to mpt.reset())
	 * @param mpt - The MPT to copy changes from
	 */
	public MPTDictionaryDelta(MPTDictionaryFull mpt) {
		//InteriorNode copiedRootOnlyChanges = (InteriorNode) MPTDictionaryDelta.copyChangesOnlyHelper(mpt.root);
		InteriorNode copiedRootOnlyChanges = (InteriorNode) MPTDictionaryDelta.copyChangesOnlyHelperRoot(mpt.root);
		this.root = copiedRootOnlyChanges;
	}
	
	//here we assume that this is a root node, i.e. an InteriorNode!
	private static Node copyChangesOnlyHelperRoot(final Node currentNode) {
		
		Node leftChild = MPTDictionaryDelta.copyChangesOnlyHelper(currentNode.getLeftChild());
		Node rightChild = MPTDictionaryDelta.copyChangesOnlyHelper(currentNode.getRightChild());
		return new InteriorNode(leftChild, rightChild);
		
	}

	private static Node copyChangesOnlyHelper(final Node currentNode) {
		//System.out.println("In MPTDictionaryDelta: copy changes for node " + currentNode);
		if(!currentNode.changed()) {
			//System.out.println("creating stub in copyChangesOnlyHelper");
			return new Stub(currentNode.getHash());
		}
		if (currentNode.isLeaf()) {
			if (currentNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			if (currentNode.changed()) {
				return new DictionaryLeafNode(currentNode.getKey(), currentNode.getValue());
			}
			return new Stub(currentNode.getHash());
		}
		Node leftChild = MPTDictionaryDelta.copyChangesOnlyHelper(currentNode.getLeftChild());
		Node rightChild = MPTDictionaryDelta.copyChangesOnlyHelper(currentNode.getRightChild());
		return new InteriorNode(leftChild, rightChild);
	}

	@Override
	public MptSerialization.MerklePrefixTrie getUpdates(final byte[] key) {
		List<byte[]> keys = new ArrayList<byte[]>();
		keys.add(key);
		return this.getUpdates(keys);
	}
	
	@Override
	public MptSerialization.MerklePrefixTrie getUpdates(final List<byte[]> keys) {
		MptSerialization.Node root = MPTDictionaryDelta.getUpdatesHelper(keys, -1, this.root);
		MptSerialization.MerklePrefixTrie tree = MptSerialization.MerklePrefixTrie.newBuilder()
				.setRoot(root)
				.build();
		return tree;
	}
	
	private static MptSerialization.Node getUpdatesHelper(final List<byte[]> matchingKeys, 
			final int currentBitIndex, final Node currentNode){
		// case: stub - this location has not changed 
		// 				--> avoid re-transmitting it by caching it on the client 
		if(currentNode.isStub()) {
			return null;
		}
		// case: non-stub - this location has changed 
		// subcase: no matching keys - value is not needed
		if(matchingKeys.size() == 0) {
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
		for(byte[] key : matchingKeys) {
			final boolean bit = Utils.getBit(key, currentBitIndex + 1);
			if(bit) {
				matchRight.add(key);
			}else {
				matchLeft.add(key);
			}
		}
		MptSerialization.Node left = MPTDictionaryDelta.getUpdatesHelper(matchLeft, currentBitIndex+1, 
				currentNode.getLeftChild());
		MptSerialization.Node right = MPTDictionaryDelta.getUpdatesHelper(matchRight, currentBitIndex+1, 
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
		return "<MPTDictionaryDelta \n"+MPTDictionaryFull.toStringHelper("+", this.root)+"\n>";
	}

}
