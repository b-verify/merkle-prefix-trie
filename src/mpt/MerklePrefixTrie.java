package mpt;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import crpyto.CryptographicDigest;

import serialization.MptSerialization;

/**
 * A  Merkle Prefix Trie (MPT) to implement a Persistent Authenticated Dictionary
 *
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public class MerklePrefixTrie {
	
	private static final Logger LOGGER = Logger.getLogger( MerklePrefixTrie.class.getName() );
	
	// we require that the root is always an interior node 
	private InteriorNode root;
	
	/**
	 * Create an empty Merkle Prefix Trie
	 */
	public MerklePrefixTrie() {
		this.root = new InteriorNode(new EmptyLeafNode(), new EmptyLeafNode());
	}
	
	/**
	 * Create a Merkle Prefix Trie with the root. This constructor is 
	 * private because it assumes that the internal structure of root is correct. 
	 * This is not safe to expose to clients.
	 */
	private MerklePrefixTrie(InteriorNode root) {
		this.root = root;
	}
	
	/**
	 * Add a (key, value) mapping to the MPT. 
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return true if the trie was modified (i.e. if the value for the key was updated)
	 */
	public boolean set(byte[] key, byte[] value){
		LeafNode leafNodeToAdd = new LeafNode(key, value);
		LOGGER.log(Level.FINE, "set("+Utils.byteArrayAsHexString(key)+") = "+
				Utils.byteArrayAsHexString(value));
		Node newRoot = this.insertLeafNodeAndUpdate(this.root, leafNodeToAdd, 0);
		boolean updated = !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return updated;
	}
		
	/**
	 * Recursive helper function to insert a leaf into the MPT. 
	 * @param node - current location in the MPT
	 * @param nodeToAdd - LeafNode node we want to add 
	 * @param currentBitIndex - the current bit index. An int such that 
	 * current location is nodeToAdd.getKeyHash()[:currentBitIndex]
	 * @return passes back up updated versions of the node, once the nodeToAdd has 
	 * been added
	 */
	private Node insertLeafNodeAndUpdate(Node node, LeafNode nodeToAdd, int currentBitIndex) {
		// if we hit a stub throw an error 
		if(node.isStub()) {
			throw new RuntimeException("tried to insert into a MPT but hit a stub");
		}
		if(node.isLeaf()) {
			// if node is an empty leaf we can just replace it 
			if(node.isEmpty()) {
				return nodeToAdd;
			}
			// this node is already in the tree
			if(Arrays.equals(node.getKeyHash(), nodeToAdd.getKeyHash())) {
				return nodeToAdd;
			}
			// otherwise have to split
			LeafNode ln = (LeafNode) node;
			return this.split(ln, nodeToAdd, currentBitIndex);
		}
		boolean bit = Utils.getBit(nodeToAdd.getKeyHash(), currentBitIndex);
		/*
		 * Encoding: if bit is 1 -> go right 
		 * 			 if bit is 0 -> go left
		 */
		if(bit) {
			Node result = this.insertLeafNodeAndUpdate(node.getRightChild(), nodeToAdd, currentBitIndex+1);
			return new InteriorNode(node.getLeftChild(), result);
		}
		Node result = this.insertLeafNodeAndUpdate(node.getLeftChild(), nodeToAdd, currentBitIndex+1);
		return new InteriorNode(result, node.getRightChild());
	}
	
	/**
	 * Recursive helper function to split two leaves which are currently mapped to the same
	 * location the MPT (have the same prefix).
	 * @param a - the first leaf
	 * @param b - the second leaf
	 * @param currentBitIndex - a and b have the same prefix (collision) at least
	 *  up to currentBitIndex - 1
	 * 	a.getKeyHash()[:currentBitIndex-1] == b.getKeyKash()[:currentBitIndex-1]
	 * @return
	 */
	private Node split(LeafNode a, LeafNode b, int currentBitIndex) {
		assert !Arrays.equals(a.getKeyHash(), b.getKeyHash());
		boolean bitA = Utils.getBit(a.getKeyHash(), currentBitIndex);
		boolean bitB = Utils.getBit(b.getKeyHash(), currentBitIndex);
		// still collision, split again
		if(bitA == bitB) {
			// recursively split 
			Node res;
			if(bitA) {
				// if bit is 1 add on the right 
				res = split(a, b, currentBitIndex+1);
				return new InteriorNode(new EmptyLeafNode(), res);
			}
			// if bit is 0 add on the left
			res = split(a, b, currentBitIndex+1);
			return new InteriorNode(res, new EmptyLeafNode());
		}
		// no collision
		if(bitA) {
			// bitA is 1, bitB is 0
			return new InteriorNode(b, a);
		}
		// bitA is 0, bitB is 1
		return new InteriorNode(a, b);
	}
	
	/**
	 * Get the value associated with a given key. Returns null if the key 
	 * is not in the MPT. 
	 * @param key
	 * @return the value if the key is in the MPT and null if the key is not.
	 * @throws IncompleteMPTException - thrown if parts of the MPT are missing such
	 * that the search cannot be completed.
	 * (E.g. if a MPT conatins a single path, and searching for this 
	 * key's prefix is not possible with just the path).
	 */
	public byte[] get(byte[] key) throws IncompleteMPTException {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "get H("+key+"): "+Utils.byteArrayAsBitString(keyHash));
		return this.getKeyHelper(this.root, keyHash, 0);
	}
	
	/**
	 * Recursive helper function to search for the keyHash by checking 
	 * progressively longer prefixes in the MPT. 
	 * Returns when it finds a leaf. If the 
	 * key is not in the tree it will eventually hit an EmptyLeafNode or a 
	 * LeafNode with a different keyHash. In this case this function will return null.
	 * @param currentNode
	 * @param keyHash
	 * @param currentBitIndex
	 * @throws IncompleteMPTException - if we attempt to go down a path
	 * we do not have (i.e. currently represent by only a stub)
	 * @return
	 */
	private byte[] getKeyHelper(Node currentNode, byte[] keyHash, 
			int currentBitIndex) throws IncompleteMPTException {
		if(currentNode.isStub()) {
			throw new IncompleteMPTException("encountered a stub - cannot complete search");
		}
		if(currentNode.isLeaf()) {
			if(!currentNode.isEmpty()) {
				// if the current node is NonEmpty and matches the Key
				if(Arrays.equals(currentNode.getKeyHash(), keyHash)){
					return currentNode.getValue();
				}
			}
			// otherwise key not in the MPT - return null;
			return null;
		}
		boolean bit = Utils.getBit(keyHash, currentBitIndex);
		if(bit) {
			return this.getKeyHelper(currentNode.getRightChild(), keyHash, currentBitIndex+1);
		}
		return this.getKeyHelper(currentNode.getLeftChild(), keyHash, currentBitIndex+1);
	}
	
	/**
	 * @christina - Not sure if this should be a public method 
	 * This method returns the node at a given location (prefix) in the MPT.
	 * If the node is not present it returns null. If the MPT is incomplete
	 * (e.g. that the search cannot be completed) it throws an exception
	 * @param fullPath - the full path 
	 * @param prefixEndIdx - an integer such that the prefix (location) of the desired 
	 * 					node is fullPath[:prefixEndIdx]
	 * @return the node at the prefix or null if it is not in the MPT
	 * @throws IncompleteMPTException - if the search cannot be completed 
	 */
	public Node getNodeAtPrefix(byte[] fullPath, int prefixEndIdx) throws IncompleteMPTException {
		return this.getNodeAtPrefixHelper(this.root, fullPath, prefixEndIdx+1, 0);
	}
	
	private Node getNodeAtPrefixHelper(Node currentNode, byte[] fullPath, int prefixEndIdx, 
			int currentIdx) throws IncompleteMPTException {
		if(currentIdx == prefixEndIdx) {
			return currentNode;
		}
		if(currentNode.isStub()) {
			throw new IncompleteMPTException("encountered a stub before could reach prefix");
		}
		// if encounter a premature leaf - then search is over 
		if(currentNode.isLeaf()) {
			return null;
		}
		boolean bit = Utils.getBit(fullPath, currentIdx);
		if(bit) {
			return this.getNodeAtPrefixHelper(currentNode.getRightChild(), fullPath, prefixEndIdx, 
					currentIdx+1);
		}
		return this.getNodeAtPrefixHelper(currentNode.getLeftChild(), fullPath, prefixEndIdx, 
				currentIdx+1);		
	}
	
	/**
	 * Remove the key from the MPT, removing any (key, value) mapping if 
	 * it exists. 
	 * @param key - the key to remove
	 * @return true if the trie was modified (i.e. if the key was previously in the tree).
	 */
	public boolean deleteKey(byte[] key) {
		byte[] keyHash = CryptographicDigest.digest(key);
		LOGGER.log(Level.FINE, "delete("+Utils.byteArrayAsHexString(key)+") - Hash= "+Utils.byteArrayAsBitString(keyHash));
		Node newRoot = this.deleteKeyHelper(this.root, keyHash, 0);
		boolean changed =  !newRoot.equals(this.root);
		this.root = (InteriorNode) newRoot;
		return changed;
	}
	
	private Node deleteKeyHelper(Node currentNode, 
			byte[] keyHash, int currentBitIndex) {
		if(currentNode.isStub()) {
			throw new RuntimeException("hit a stub when trying to delete key");
		}
		if(currentNode.isLeaf()) {
			if(!currentNode.isEmpty()) {
				if(Arrays.equals(currentNode.getKeyHash(), keyHash)){
					return new EmptyLeafNode();
				}
			}
			// otherwise the key is not in the tree and nothing needs to be done
			return currentNode;
		}
		// we have to watch out to make sure that if this is the root node
		// that we return an InteriorNode and don't propagate up an empty node
		boolean isRoot = currentNode.equals(this.root);
		boolean bit = Utils.getBit(keyHash, currentBitIndex);
		Node leftChild = currentNode.getLeftChild();
		Node rightChild = currentNode.getRightChild();
		if(bit) {
			// delete key from the right subtree
			Node newRightChild = this.deleteKeyHelper(rightChild, keyHash, currentBitIndex+1);
			// if left subtree is empty, and rightChild is leaf
			// we push the newRightChild back up the MPT
			if(leftChild.isEmpty() && newRightChild.isLeaf() && !isRoot) {
				return newRightChild;
			}
			// if newRightChild is empty, and leftChild is a leaf
			// we push the leftChild back up the MPT
			if(newRightChild.isEmpty() && leftChild.isLeaf() && !isRoot) {
				return leftChild;
			}
			// otherwise update the interior node
			return new InteriorNode(leftChild, newRightChild);
		}
		Node newLeftChild = this.deleteKeyHelper(leftChild, keyHash, currentBitIndex+1);
		if(rightChild.isEmpty() && newLeftChild.isLeaf() && !isRoot) {
			return newLeftChild;
		}
		if(newLeftChild.isEmpty() && rightChild.isLeaf() && !isRoot) {
			return rightChild;
		}
		return new InteriorNode(newLeftChild, rightChild);
	}
	
	/**
	 * Get the cryptographic commitment to this set of 
	 * (key, value) pairs
	 * @return
	 */
	public byte[] getCommitment() {
		return this.root.getHash();
	}
	
	/**
	 * Returns a new MPT that contains path that maps the key 
	 * to a (possibly empty) leaf in this MPT. 
	 * The new MPT has the leaf entry as well as the hashes 
	 * on the path (in the form of stubs) needed to authenticate it
	 * @param - a key, arbitrary bytes, may or may not be in the tree
	 * @return - a MerklePrefixTrie containing only the path 
	 * mapping the key to a leaf
	 */
	public MerklePrefixTrie copyPath(byte[] key) {
		int currBitIndex = 0;
		byte[] keyHash = CryptographicDigest.digest(key);
		Node res = this.pathBuilder(this.root, keyHash, currBitIndex);
		InteriorNode newRoot = (InteriorNode) res;
		return new MerklePrefixTrie(newRoot);
	}
	
	/**
	 * Recursive helper function for building a path to a leaf specified by a keyHash
	 * @param currNode the node in the trie we are copying from, located at the position
	 * keyHash[:currentIndex] 
	 * @param keyHash - the hash of the key (path in the MPT) to copy 
	 * @param currentIndex int s.t. path to currNode from root is keyHash[:currIndex]
	 * @return Node a Node with children that form the path to a leaf in this tree
	 */
	private Node pathBuilder(Node currNode, byte[] keyHash, int currIndex) {
		if(currNode.isStub()) {
			throw new RuntimeException("hit a stub - cannot copy");
		}
		// base case
		if (currNode.isLeaf()) {
			if (currNode.isEmpty()) {
				// empty leaf (key not in MPT)
				return new EmptyLeafNode();
			} 
			// leaf node (key possibly in MPT, only if currNode.getKeyHash() == keyHash)
			return new LeafNode(currNode.getKey(), currNode.getValue());			
		}	
		//recursive step
		boolean bit = Utils.getBit(keyHash, currIndex);
		if (bit) {
			//bit == 1 -> go right
			Node childOnPath = this.pathBuilder(currNode.getRightChild(), keyHash, currIndex + 1);
			// ignore path on left - stub
			Node leftStub = new Stub(currNode.getLeftChild().getHash());
			return new InteriorNode(leftStub, childOnPath);
		} 
		//bit == 0 -> go left
		Node childOnPath = this.pathBuilder(currNode.getLeftChild(), keyHash, currIndex + 1);
		// ignore path on right - stub
		Node rightStub = new Stub(currNode.getRightChild().getHash());
		return  new InteriorNode(childOnPath, rightStub);
	}
	
	/**
	 * Efficiently serializes this MPT. The serialization
	 * contains the minimal set of information required to reconstruct 
	 * and authenticate this MPT when deserialized on the client.
	 * @return
	 */
	public byte[] serialize() {
		MptSerialization.Node rootSerialization = this.root.serialize();
		MptSerialization.MerklePrefixTrie.Builder builder = 
				MptSerialization.MerklePrefixTrie.newBuilder();
		builder.setRoot(rootSerialization);
		return builder.build().toByteArray();
	}
	
	/**
	 * Parses a MPT from a byte serialization. Throws an 
	 * InvalidMPTSerialization if the tree cannot be deserialized properly
	 * @param asbytes
	 * @return
	 * @throws InvalidMPTSerialization
	 */
	public static MerklePrefixTrie deserialize(byte[] asbytes) throws 
	InvalidMPTSerializationException	{
		MptSerialization.MerklePrefixTrie mptProof;
		try {
			mptProof = MptSerialization.MerklePrefixTrie.parseFrom(asbytes);
		}catch(InvalidProtocolBufferException e) {
			throw new InvalidMPTSerializationException(e.getMessage());
		}
		if(!mptProof.hasRoot()) {
			throw new InvalidMPTSerializationException("no root included");
		}
		Node root = MerklePrefixTrie.parseNode(mptProof.getRoot());
		if(! ( root instanceof InteriorNode )) {
			throw new InvalidMPTSerializationException("root is not an interior node!");
		}
		InteriorNode rootInt = (InteriorNode) root;
		return new MerklePrefixTrie(rootInt);
	}
	
	/**
	 * Helper function with recursively parses the individual serialized 
	 * nodes into MerklePrefixTrie nodes according to the mpt.proto format.
	 * Throws InvalidMPTSerialization if a node is not correctly formatted
	 * and cannot be parsed
	 * @param nodeSerialization
	 * @return
	 * @throws InvalidMPTSerialization
	 */
	private static Node parseNode(MptSerialization.Node nodeSerialization) throws 
	InvalidMPTSerializationException {
		switch(nodeSerialization.getNodeCase()) {
		case INTERIOR_NODE :
			MptSerialization.InteriorNode in = nodeSerialization.getInteriorNode();
			Node left, right;
			/*
			 * If an interior node child is not present, we assume it is an empty child
			 */
			if(!in.hasLeft()) {
				left = new EmptyLeafNode();
			}else {
				left = MerklePrefixTrie.parseNode(in.getLeft());
			}
			if(!in.hasRight()) {
				right = new EmptyLeafNode();
			}else {
				right = MerklePrefixTrie.parseNode(in.getRight());
			}
			return new InteriorNode(left, right);
		case STUB :
			MptSerialization.Stub stub = nodeSerialization.getStub();
			if(stub.getHash().isEmpty()) {
				throw new InvalidMPTSerializationException("stub doesn't have a hash");
			}
			return new Stub(stub.getHash().toByteArray());
		case LEAF : 
			MptSerialization.Leaf leaf = nodeSerialization.getLeaf();
			if(leaf.getKey().isEmpty() || leaf.getValue().isEmpty()) {
				throw new InvalidMPTSerializationException("leaf doesn't have required keyhash and value");
			}
			return new LeafNode(leaf.getKey().toByteArray(), leaf.getValue().toByteArray());
		case NODE_NOT_SET : 
			throw new InvalidMPTSerializationException("no node included - fatal error");
		}
		return null;
	}
	
	public void deserializeUpdates(byte[] updateBytes) throws InvalidMPTSerializationException{
		try {
			MptSerialization.MerklePrefixTrieUpdate updateMsg = 
					MptSerialization.MerklePrefixTrieUpdate.parseFrom(updateBytes);
			List<MptSerialization.Update> updates = updateMsg.getUpdatesList();
			for(MptSerialization.Update update : updates) {
				Node newStubOrLeaf = MerklePrefixTrie.parseNode(update.getNode());
				if(!(newStubOrLeaf instanceof LeafNode || newStubOrLeaf instanceof Stub)) {
					throw new InvalidMPTSerializationException("tried to insert something "
							+ "other than a stub or leaf");
				}
				// perform update
				Node newRoot = this.updateStubOrLeafHelper(this.root, 
						newStubOrLeaf, update.getFullPath().toByteArray(), 
						update.getIndex()+1, 0);
				// update the root (save the update)
				this.root = (InteriorNode) newRoot;
				
			}
		}catch(InvalidProtocolBufferException e) {
			throw new InvalidMPTSerializationException(e.getMessage());
		}
	}
	
	/**
	 * Recursive helper function. Given a stub or leaf and a prefix. This function 
	 * replaces the stub or leaf at the location of prefix with the updated stub or leaf.
	 * If the node at that prefix does not exist, or is not a stub or a leaf this 
	 * method throws an error 
	 * @param currentLocation - current location in the trie
	 * @param newStubOrLeaf - stub or leaf to insert
	 * @param path - full hash of node 
	 * @param prefixEndIdx - end index - defines a prefix of path[:prefixEndIndx] - 
	 * 						which corresponds to a location in the MPT
	 * @param currentIdx - current location in the search path[:currentIndx]
	 * @return
	 * @throws InvalidMPTSerializationException
	 */
	private Node updateStubOrLeafHelper(Node currentLocation, Node newStubOrLeaf, byte[] path, int prefixEndIdx, 
			int currentIdx) throws InvalidMPTSerializationException{
		// if we are at the correct location in the MPT
		if(currentIdx == prefixEndIdx) {
			// both the node we are inserting and the node
			// we are replacing should be a stub or leaf
			if((currentLocation.isStub() || currentLocation.isLeaf()) && 
					(newStubOrLeaf.isStub() || newStubOrLeaf.isLeaf())) {
				return newStubOrLeaf;
			}
			throw new InvalidMPTSerializationException("Both the node being and the node "
					+ "being replaced should be stub or leaf");
		}
		// this shouldn't happen since we are not at the correct location in the MPT yet
		if(currentLocation.isLeaf()) {
			throw new InvalidMPTSerializationException("tried to insert at a path "
					+ "not currently present in the MPT");
		}
		Node left = currentLocation.getLeftChild();
		Node right = currentLocation.getRightChild();
		boolean bit = Utils.getBit(path, currentIdx);
		if(bit) {
			Node newRight = this.updateStubOrLeafHelper(right, newStubOrLeaf,
					path, prefixEndIdx, currentIdx+1);
			return new InteriorNode(left, newRight);
		}
		Node newLeft = this.updateStubOrLeafHelper(left,
				newStubOrLeaf, path, prefixEndIdx, currentIdx+1);
		return new InteriorNode(newLeft, right);
	}
	
	/**
	 * Returns the height of the tree. Height is defined 
	 * as the maximum possible distance from the leaf to the root node
	 * (TODO: I'm not sure this should be a public method - only really 
	 *  useful for benchmarking purposes)
	 * @return
	 * @throws IncompleteMPTException
	 */
	public int getMaxHeight() throws IncompleteMPTException{
		return this.getHeightRecursive(this.root);
	}
		
	private int getHeightRecursive(Node currentLocation) throws IncompleteMPTException {
		// if we encounter a stub we do not have the entire tree
		// so we cannot determine the height
		if (currentLocation.isStub()) {
			throw new IncompleteMPTException("stub encountered - cannot determine height of tree");
		}
		// each leaf is at height zero
		if (currentLocation.isLeaf()) {
			return 0;
		}
		// otherwise we are at an interior node - height is maximum of 
		// the height of the children plus 1
		return Math.max(
				this.getHeightRecursive(currentLocation.getLeftChild()), 
				this.getHeightRecursive(currentLocation.getRightChild())) + 1;
	}
	
	
	@Override
	public String toString() {
		return this.toStringHelper("+", this.root);
	}
	
	private String toStringHelper(String prefix, Node node) {
		String result = prefix+" "+node.toString();
		if(!node.isLeaf() && !node.isStub()) {
			String left = this.toStringHelper(prefix+"0", node.getLeftChild());
			String right = this.toStringHelper(prefix+"1", node.getRightChild());
			result = result+"\n"+left+"\n"+right;
		}
		return result;
	}
	
	/**
	 * Two MPTs are equal if they are STRUCTURALLY IDENTICAL which means
	 * two trees are equal if the contain identical nodes arranged in the
	 * same way.
	 */
	@Override
	public boolean equals(Object other) {
		if(other instanceof MerklePrefixTrie) {
			MerklePrefixTrie othermpt = (MerklePrefixTrie) other;
			return this.root.equals(othermpt.root);
		}
		return false;
	}
}
