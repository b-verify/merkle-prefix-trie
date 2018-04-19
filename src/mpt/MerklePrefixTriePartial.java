package mpt;

import java.util.Arrays;

import crpyto.CryptographicDigest;

public class MerklePrefixTriePartial {
	
	private InteriorNode root;

	public MerklePrefixTriePartial(MerklePrefixTrie fullMPT) {
		// just copies the root
		this.root = new InteriorNode(new Stub(fullMPT.root.getLeftChild().getHash()),
				new Stub(fullMPT.root.getRightChild().getHash()));
	}
	
	public void addPath(MerklePrefixTrie fullMPT, byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		Node newRoot = MerklePrefixTriePartial.addPathHelper(this.root, fullMPT.root, keyHash, -1);
		this.root = (InteriorNode) newRoot;
	}
	
	private static Node addPathHelper(final Node thisNode, 
			final Node copyNode, final byte[] keyHash, int currentBitIndex) {
		if(copyNode.isLeaf()) {
			if(copyNode.isEmpty()) {
				return new EmptyLeafNode();
			}
			if(Arrays.equals(copyNode.getKeyHash(), keyHash)) {
				return new LeafNode(copyNode.getKey(), copyNode.getValue());				
			}
			return new Stub(copyNode.getHash());
		}
		Node leftChild = copyNode.getLeftChild();
		Node rightChild = copyNode.getRightChild();
		Node thisLeftChild = null;
		Node thisRightChild = null;
		if(thisNode != null) {
			if(!thisNode.isStub()) {
				thisLeftChild = thisNode.getLeftChild();
				thisRightChild = thisNode.getRightChild();
			}
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		if(bit) {
			rightChild = MerklePrefixTriePartial.addPathHelper(
					thisLeftChild, copyNode.getRightChild(), keyHash, currentBitIndex+1);
			// use this left child if possible
			if(thisLeftChild != null) {
				leftChild = thisLeftChild;
			}
		}else {
			leftChild = MerklePrefixTriePartial.addPathHelper(
					thisRightChild, copyNode.getLeftChild(), keyHash, currentBitIndex+1); 
			if(thisRightChild != null) {
				rightChild = thisRightChild;
			}
		}
		return new InteriorNode(leftChild, rightChild);
	}
	
	@Override
	public String toString() {
		return "<MerklePrefixTriePartial \n"+MerklePrefixTrie.toStringHelper("+", this.root)+"\n>";
	}
}
