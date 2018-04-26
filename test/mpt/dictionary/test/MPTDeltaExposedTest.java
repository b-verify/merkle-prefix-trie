package mpt.dictionary.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import mpt.core.Node;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;

public class MPTDeltaExposedTest {
	
	private class MPTDeltaExposed extends MPTDictionaryDelta {

		private MPTDeltaExposed(MPTDictionaryFull mpt) {
			super(mpt);
			// TODO Auto-generated constructor stub
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
	
	public static byte[] getByteArray(Set<Byte> toAdd) {
		
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
	 * Testing strategy
	 * 
	 * 
	 */
	
	@Test
	public void testDeltaEmptyNoReset() {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		//System.out.println("FRESH");
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		System.out.println(delta);
		//TODO check specs for this - what do with empty delta with no reset?
		trie.reset();
		
		delta = new MPTDeltaExposed(trie);
		System.out.println(delta);
		
		
		
	}
	
	@Test
	public void testDeltaEmptyAfterReset() {
		
		
	}
	
	@Test
	public void testDeltaInsertThenReset() {
		
	}

	@Test
	public void testDeltaAfterPlay() {
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>();
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(SIX);
		byte[] key2 = getByteArray(second);
		
		trie.insert(key1, key1);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		System.out.println("ONE");
		System.out.println(delta);
		
		trie.reset();
		
		delta = new MPTDeltaExposed(trie);
		System.out.println("TWO");
		System.out.println(delta);
		
		trie.insert(key1, key2);
		
		delta = new MPTDeltaExposed(trie);
		System.out.println("THREE");
		System.out.println(delta);
		
		Node root = delta.getRoot();
		
	}
	
	@Test
	public void testDeltaAfterInsertSplit() {
		//11
		//10
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		Set<Byte> first = new HashSet<>();
		first.add(SEVEN);
		first.add(SIX);
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(SEVEN);
		byte[] key2 = getByteArray(second);
		second.add(FIVE);
		byte[] key3 = getByteArray(second);
		
		trie.insert(key1, key1);
		System.out.println("LALA");
		System.out.println(trie);
		trie.reset();
		
		trie.insert(key2, key2);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		System.out.println("APPLE");
		System.out.println(delta);
		System.out.println("ORIGINAL");
		System.out.println(trie);
		
		
		trie.reset();
		trie.insert(key3, key3);
		delta = new MPTDeltaExposed(trie);
		System.out.println("LAST ONE");
		System.out.println(delta);
		
	}
	
	@Test
	public void testDeltaAfterInsertLongSplit() {
		//11010
		//reset
		//11011
		
		Set<Byte> first = new HashSet<>();
		first.add(SEVEN);
		first.add(SIX);
		first.add(FOUR);
		byte[] key1 = getByteArray(first);
		first.add(THREE);
		byte[] key2 = getByteArray(first);
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key1, key2);
		trie.reset();
		trie.insert(key2, key2);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		
		Node root = delta.getRoot();
		Node left = root.getLeftChild();
		assertTrue(left.isStub());
		
		//check rest of the path
		
	}
	
	@Test
	public void testDeltaAfterInsertIntoEmptyLeaf() {
		//101
		//100
		//reset
		//11
		
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
		trie.reset();
		trie.insert(key3, key3);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		
		System.out.println(delta);
		
		Node root = delta.getRoot();
		Node index_1_0 = root.getLeftChild();
		assertTrue(index_1_0.isStub());
		
		Node index_2_3 = (root.getRightChild()).getLeftChild();
		assertTrue(index_2_3.isStub());
		
		//inspect rest of delta trie
	}
	
	@Test
	public void testDeltaAfterInsertUpdateValue() {
		
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
		trie.reset();
		trie.insert(key2, key1);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		
		//expect 010 to be stub
		Node root = delta.getRoot();
		Node stubby = ((root.getLeftChild()).getRightChild()).getLeftChild();
		assertTrue(stubby.isStub());
		
		//expect 011 not to be stub
		Node newInsert = ((root.getLeftChild()).getRightChild()).getRightChild();
		assertTrue(newInsert.isLeaf());
		assertFalse(newInsert.isEmpty());
		assertArrayEquals(key2, newInsert.getKey());
		
	}
	
	@Test
	public void testDeltaAfterInsertIdenticalMapping() {
		//1101
		//1100
		//reset
		//same insert for 1101
		
		Set<Byte> first = new HashSet<>();
		first.add(SIX);
		byte[] key1 = getByteArray(first); //010
		first.add(FIVE);
		byte[] key2 = getByteArray(first); //011
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.reset();
		trie.insert(key2, key2); //reinsert preexisting mapping
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		
		//entire tree should be stubby b/c no change to structure or value
		assertTrue(root.getRightChild().isStub());
		assertTrue(root.getLeftChild().isStub()); //TODO clarify specs
		
	}
	
	
	@Test
	public void testDeltaAfterDeleteShrink() {
		
		//insert 01000000
		//insert 00100000
		//insert 00000000
		//reset
		//delete 0010000
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first);
		first.add(SIX);
		byte[] key1 = getByteArray(first); //010
		first.remove(SIX);
		first.add(FIVE);
		byte[] key2 = getByteArray(first); //001
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key0);
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.reset();
		trie.delete(key2);
		

		MPTDeltaExposed delta = new MPTDeltaExposed(trie);		
		Node root = delta.getRoot();
		Node prefix_01 = (root.getLeftChild()).getRightChild();
		//01 should be stub
		assertTrue(prefix_01.isStub());
		
		//check others are not stub
		
	}
	
	
	@Test
	public void testDeltaAfterDeleteBigShrink() {
		//01000000
		//00000001
		//00000000
		
		//reset
		//delete 00000001
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first);
		first.add(SIX);
		byte[] key1 = getByteArray(first); //010
		first.remove(SIX);
		first.add(ONE);
		byte[] key2 = getByteArray(first); //001
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key0);
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.reset();
		trie.delete(key2);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		//expect right child of root to be stub
		assertTrue(root.getRightChild().isStub());
		
		
		//expect left child of root to be leaf
		assertFalse(root.getLeftChild().isStub());
		assertTrue(root.getLeftChild().isLeaf());
		assertFalse(root.getLeftChild().isEmpty());
		
		
	}
	
	@Test
	public void testDeltaAfterDeleteRemoveChildLeafNoShrinking() {
		
		//001
		//000011
		//000010
		
		//reset
		//delete 001
		
	}
	
	
	@Test
	public void testDeltaAfterDeletingNonexistentKey() {
		
		//generate some tree, maybe full?
		//reset
		//try deleting key that doesn't exist
		//check that delta trie is completely stub
		
	}
	
	
}
