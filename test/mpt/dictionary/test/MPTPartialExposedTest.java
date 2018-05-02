package mpt.dictionary.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import mpt.core.InvalidSerializationException;
import mpt.core.Node;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;
import serialization.generated.MptSerialization;

public class MPTPartialExposedTest {
	
	private class MPTPartialExposed extends MPTDictionaryPartial {

		private MPTPartialExposed(MPTDictionaryFull fullMPT, List<byte[]> keys) {
			super(fullMPT, keys);
		}
		
		private Node getRoot() {
			return this.root;
		}
		
	}
	
	private static final int SIZE_BITS = 256;
	private static final int SIZE_BYTES = SIZE_BITS / 8;
	private static final byte SEVEN = (byte) Math.pow(2, 7); //byte array 1000...
	private static final byte SIX = 64;
	private static final byte FIVE = 32;
	private static final byte FOUR = 16;
	private static final byte THREE = 8;
	private static final byte TWO = 4;
	private static final byte ONE = 2;
	private static final byte ZERO = 1;
	
	/**
	 * Return byte[] of size 32, and first byte is sum of bytes in toAdd, and rest is all 0
	 * @param sum
	 * @return
	 */
	private static byte[] getByteArray(Set<Byte> toAdd) {
		
		byte[] result = new byte[SIZE_BYTES];
		byte sum = 0;
		for (Byte b : toAdd) {
			sum += b;
		}
		result[0] = sum;
		
		for (int i = 1; i < SIZE_BYTES; i++) {
			result[i] = 0;
		}
		
		return result;
		
	}
	
	/**
	 * Testing generating partial tries for one key
	 */
	
	@Test
	public void testPartialNonexistentKey() {
		
		Set<Byte> first = new HashSet<>();
		byte[] rogueKey = getByteArray(first); //00
		first.add(SIX);
		byte[] key0 = getByteArray(first); //01
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //11
		first.remove(SIX);
		byte[] key2 = getByteArray(first); //10
		
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0	, key1);
		trie.insert(key1, key2);
		trie.insert(key2, key0);
		
		
		List<byte[]> keys = new ArrayList<>();
		keys.add(rogueKey);
		
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		//System.out.println("PARTIAL");
		//System.out.println(partial);
		
		Node root = partial.getRoot();
		
		assertTrue(root.getLeftChild().isLeaf());
		assertTrue(root.getRightChild().isStub());
		
	}
	
	@Test
	public void testPartialEmptyKeys() {
		
		Set<Byte> first = new HashSet<>();
		byte[] rogueKey = getByteArray(first); //00
		first.add(SIX);
		byte[] key0 = getByteArray(first); //01
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //11
		first.remove(SIX);
		byte[] key2 = getByteArray(first); //10
		
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0	, key1);
		trie.insert(key1, key2);
		trie.insert(key2, key0);
		
		List<byte[]> keys = new ArrayList<>();
		
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		Node root = partial.getRoot();
		assertTrue(root.isStub());
		
	}
	
	@Test
	public void testPartialBasicOneKey() {
		
		Set<Byte> first = new HashSet<>();
		first.add(SIX);
		byte[] key0 = getByteArray(first); //01
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //11
		first.remove(SIX);
		byte[] key2 = getByteArray(first); //10
		
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0	, key1);
		trie.insert(key1, key2);
		trie.insert(key2, key0);
		
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1); //11
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys); 
		
		System.out.println("PARTIAL");
		System.out.println(partial);
		
		Node root = partial.getRoot();
		assertTrue(root.getLeftChild().isStub());
		Node path_10 = root.getRightChild().getLeftChild();
		Node path_11 = root.getRightChild().getRightChild();
		assertTrue(path_10.isStub());
		assertTrue(path_11.isLeaf());
	}
	
	@Test
	public void testPartialOneKeyDeep() {
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //0
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //1000
		first.add(SIX);
		byte[] key2 = getByteArray(first); //11
		first.remove(SIX);
		first.add(FIVE);
		byte[] key3 = getByteArray(first); //101
		first.remove(FIVE);
		first.add(FOUR);
		byte[] key4 = getByteArray(first); //1001
		
		
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		//trie with:
		//0
		//1000
		//1001
		//101
		//11
		trie.insert(key0, key0);
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.insert(key3, key3);
		trie.insert(key4, key4);
		
		List<byte[]> keys = new ArrayList<>();
		keys.add(key4);
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		System.out.println("PARTIAL");
		System.out.println(partial);
		
		Node root = partial.getRoot();
		assertTrue(root.getLeftChild().isStub());
		
		Node path_11 = root.getRightChild().getRightChild();
		Node path_101 = root.getRightChild().getLeftChild().getRightChild();
		Node path_1000 = root.getRightChild().getLeftChild().getLeftChild().getLeftChild();
		Node path_1001 = root.getRightChild().getLeftChild().getLeftChild().getRightChild();
		assertTrue(path_11.isStub());
		assertTrue(path_101.isStub());
		assertTrue(path_1000.isStub());
		assertTrue(path_1001.isLeaf());
		
	}
	
	@Test
	public void testPartialManyKeysUnbalancedTrie() {
		
		//10
		//1101
		//1100
		//0001
		//0000
		//001
		
		Set<Byte> first = new HashSet<>();
		byte[] key4 = getByteArray(first); //0
		first.add(SEVEN);
		byte[] key0 = getByteArray(first); //10
		first.add(SIX);
		byte[] key1 = getByteArray(first); //1100
		first.add(FOUR);
		byte[] key2 = getByteArray(first); //1101
		first.clear();
		first.add(FIVE);
		byte[] key3 = getByteArray(first); //001
		first.clear();
		first.add(FOUR);
		byte[] key5 = getByteArray(first); //0001
		
		List<byte[]> keys = new ArrayList<>();
		keys.add(key4);//0
		keys.add(key3); //001
		keys.add(key0); //10
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0, key0);
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.insert(key3, key3);
		trie.insert(key4, key4);
		trie.insert(key5, key5);
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		Node root = partial.getRoot();
		
		assertTrue(root.getLeftChild().getLeftChild().getLeftChild().getLeftChild().isLeaf());
		assertTrue(root.getLeftChild().getLeftChild().getLeftChild().getRightChild().isStub());
		assertTrue(root.getLeftChild().getLeftChild().getRightChild().isLeaf());
		assertTrue(root.getRightChild().getRightChild().isStub());
		assertTrue(root.getRightChild().getLeftChild().isLeaf());
	}
	
	/**
	 * Testing processUpdates for single key
	 */
	
	@Test
	public void testProcessUpdatesNoChange() throws InvalidSerializationException {
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		//System.out.println("FRESH");
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		System.out.println(delta);
		//TODO check specs for this - what do with empty delta with no reset?
		trie.reset();
		
		delta = new MPTDictionaryDelta(trie);
		System.out.println(delta);
		
		List<byte[]> keys = new ArrayList<>();
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		partial.processUpdates(deltaSerialized);
		
		System.out.println(partial);
		
		assertTrue(partial.getRoot().isStub());
		
	}

	@Test
	public void testProcessUpdatesInsertSplit() throws InvalidSerializationException {
		
		//insert 11
		//reset 
		//insert 10
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>();
		first.add(SEVEN);
		first.add(SIX);
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(SEVEN);
		byte[] key2 = getByteArray(second);
		
		trie.insert(key1, key1);
		
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		trie.reset();
		
		trie.insert(key2, key2);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
		System.out.println(partial);
		
		//11 stub
		assertTrue(root.getRightChild().getRightChild().isLeaf());
		
		//10 leaf
		assertTrue(root.getRightChild().getLeftChild().isStub());
		
		//0 empty leaf or stub?
		
	}
	
	@Test
	public void testProcessUpdatesInsertIntoEmptyLeaf() throws InvalidSerializationException {
		
		Set<Byte> first = new HashSet<>();
		first.add(SEVEN);
		first.add(SIX);
		byte[] key3 = getByteArray(first); //11
		first.remove(SIX);
		byte[] key2 = getByteArray(first); //100
		first.add(FIVE);
		byte[] key1 = getByteArray(first); //101
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key3);
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		trie.insert(key3, key3);
		
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
		System.out.println("PARTIAL");
		System.out.println(partial);
		
		assertTrue(root.getRightChild().getRightChild().isLeaf());
		assertTrue(root.getRightChild().getLeftChild().isStub());
		
	}
	
	@Test
	public void testProcessUpdatesInsertLongSplit() throws InvalidSerializationException {
		
		////11010
		//reset
		//11011
		
		Set<Byte> first = new HashSet<>();
		first.add(SEVEN);
		first.add(SIX);
		first.add(FOUR);
		byte[] key1 = getByteArray(first); //11010
		first.add(THREE);
		byte[] key2 = getByteArray(first); //11011
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key1, key2);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);

		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		trie.reset();
		trie.insert(key2, key2);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
		Node path_11010 = root.getRightChild().getRightChild().getLeftChild().getRightChild().getLeftChild();
		assertTrue(path_11010.isLeaf());
		Node path_11011 = root.getRightChild().getRightChild().getLeftChild().getRightChild().getRightChild();
		assertTrue(path_11011.isStub());
		
	}
	
	@Test
	public void testProcessUpdatesInsertUpdateValue() throws InvalidSerializationException {
		
		//010
		//011
		//reset
		//new insert for 011
				
		Set<Byte> first = new HashSet<>();
		first.add(SIX);
		byte[] key1 = getByteArray(first); //010
		first.add(FIVE);
		byte[] key2 = getByteArray(first); //011
				
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		List<byte[]> keys = new ArrayList<>();
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		trie.insert(key2, key1);
		////
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		System.out.println("PARTIAL\n" + partial);
		
		//Node path_010 = root.getLeftChild().getRightChild().getLeftChild();
		//Node path_011 = root.getLeftChild().getRightChild().getRightChild();
		//
		//assertTrue(path_010.isStub());
		//assertTrue(path_011.isLeaf());
		//TODO make sure changes to values lead to correct partial when updated
		
	}
	
	
	
	@Test
	public void testProcessUpdatesDeleteRemoveLeafNode() throws InvalidSerializationException {
		
		//000
		//001
		//01
		
		//watch 000
		
		//remove 01
		
		//00 should be stub
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //000
		first.add(SIX);
		byte[] key1 = getByteArray(first); //010
		first.remove(SIX);
		first.add(FIVE);
		byte[] key2 = getByteArray(first); //001
				
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0, key2);
		trie.insert(key1, key1);
		trie.insert(key2, key0);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key0);
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		trie.delete(key1);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		System.out.println("PRINT");
		
		System.out.println(partial);
		
		Node root = partial.getRoot();
		
		//assertTrue(root.getLeftChild().isStub());
		//TODO 
		assertFalse(root.getLeftChild().isStub());
		
	}
	
	
	@Test
	public void testProcessUpdatesDeleteBigShrink() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>(); //11101010
		byte[] key0 = getByteArray(first);
		first.add(SEVEN);
		first.add(SIX);
		first.add(FIVE);
		first.add(THREE);
		first.add(ONE);
		
		
		byte[] key1 = getByteArray(first);
		
		first.add(ZERO);
		byte[] key2 = getByteArray(first);
		
		//insert 0
		trie.insert(key0, key2);
		//insert 11101010
		trie.insert(key1, key0);
		//insert 11101011
		trie.insert(key2, key1);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		//remove 11101011
		trie.delete(key2);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		assertTrue(root.getRightChild().isLeaf());
		assertTrue(root.getLeftChild().isStub());
		
	}
	
	/**
	 * Testing processUpdate for multiple keys
	 */
	
	@Test
	public void testProcessUpdatesChangeInOneRelevantKey() throws InvalidSerializationException {
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>(); //11101010
		byte[] key0 = getByteArray(first);
		first.add(SEVEN);
		first.add(SIX);
		first.add(FIVE);
		first.add(THREE);
		first.add(ONE);
		
		
		byte[] key1 = getByteArray(first);
		
		first.add(ZERO);
		byte[] key2 = getByteArray(first);
		
		//insert 0
		trie.insert(key0, key2);
		//insert 11101010
		trie.insert(key1, key0);
		//insert 11101011
		trie.insert(key2, key1);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		keys.add(key0);
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		//remove 11101011
		trie.delete(key2);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		assertTrue(root.getRightChild().isLeaf());
		assertTrue(root.getLeftChild().isLeaf());
	}
	
	@Test
	public void testProcessUpdatesChangeInAllRelevantKeys() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>(); //11101010
		byte[] key0 = getByteArray(first);
		first.add(SEVEN);
		first.add(SIX);
		first.add(FIVE);
		first.add(THREE);
		first.add(ONE);
		
		
		byte[] key1 = getByteArray(first);
		
		first.add(ZERO);
		byte[] key2 = getByteArray(first);
		
		//insert 0
		trie.insert(key0, key2);
		//insert 11101010
		trie.insert(key1, key0);
		//insert 11101011
		trie.insert(key2, key1);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		keys.add(key0);
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		trie.reset();
		//remove 11101011
		trie.delete(key2);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		assertTrue(root.getRightChild().isLeaf());
		assertTrue(root.getLeftChild().isLeaf());
		
	}
	
	//random question: should we order the deltas (by giving them ID #'s, etc)?
	
	
}
