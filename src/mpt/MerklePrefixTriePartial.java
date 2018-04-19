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
		System.out.println("adding path for: "+Utils.byteArrayAsBitString(keyHash));
		Node newRoot = MerklePrefixTriePartial.addPathHelper(this.root, fullMPT.root, keyHash, -1);
		this.root = (InteriorNode) newRoot;
	}
	
	private static Node addPathHelper(final Node thisNode, 
			final Node copyNode, final byte[] keyHash, int currentBitIndex) {
		System.out.println("this node: "+thisNode+" copy node: "+copyNode);
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
					thisRightChild, copyNode.getRightChild(), keyHash, currentBitIndex+1);
			// use this left child if we have one 
			if(thisLeftChild != null) {
				// and it is not a stub 
				if(!thisLeftChild.isStub()) {
					return new InteriorNode(thisLeftChild, rightChild);					
				}
			}
			return new InteriorNode(new Stub(leftChild.getHash()), rightChild);
		}
		leftChild = MerklePrefixTriePartial.addPathHelper(
				thisLeftChild, copyNode.getLeftChild(), keyHash, currentBitIndex+1); 
		if(thisRightChild != null) {
			if(!thisRightChild.isStub()) {
				return new InteriorNode(leftChild, thisRightChild);			
			}
		}
		return new InteriorNode(leftChild, new Stub(rightChild.getHash()));
	}
	
	public byte[] get(byte[] key) throws IncompleteMPTException {
		byte[] keyHash = CryptographicDigest.digest(key);
		return MerklePrefixTriePartial.getHelper(this.root, keyHash, -1);
	}

	private static byte[] getHelper(Node currentNode, byte[] keyHash, int currentBitIndex) throws IncompleteMPTException {
		if (currentNode.isStub()) {
			throw new IncompleteMPTException(
					"stub encountered at: " + Utils.byteArrayPrefixAsBitString(keyHash, currentBitIndex));
		}
		if (currentNode.isLeaf()) {
			if (!currentNode.isEmpty()) {
				// if the current node is NonEmpty and matches the Key
				if (Arrays.equals(currentNode.getKeyHash(), keyHash)) {
					return currentNode.getValue();
				}
			}
			// otherwise key not in the MPT - return null;
			return null;
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex + 1);
		if (bit) {
			return MerklePrefixTriePartial.getHelper(currentNode.getRightChild(), keyHash, currentBitIndex + 1);
		}
		return MerklePrefixTriePartial.getHelper(currentNode.getLeftChild(), keyHash, currentBitIndex + 1);
	}
	
	public byte[] commitment() {
		return this.root.getHash();
	}
	
	@Override
	public String toString() {
		return "<MerklePrefixTriePartial \n"+MerklePrefixTrie.toStringHelper("+", this.root)+"\n>";
	}
}
