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

/**
 * Test class for testing internal structure of MPTDictionaryDelta
 * 
 * @author christinalee
 *
 */
public class MPTDeltaExposedTest {
	
	/**
	 * Class to 
	 * @author christinalee
	 *
	 */
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
	
	
	/**
	 * Testing <= 1 changes since reset
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
		
		Node index_2_4 = (root.getRightChild()).getRightChild(); //prefix 11
		assertTrue(index_2_4.isLeaf());
		assertFalse(index_2_4.isEmpty());
		assertArrayEquals(key3, index_2_4.getKey());
		
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
		//TODO check rest of trie
		
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
		byte[] key0 = getByteArray(first); //00000000
		first.add(SIX);
		byte[] key1 = getByteArray(first); //01000000
		first.remove(SIX);
		first.add(ONE);
		byte[] key2 = getByteArray(first); //00000001
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key0); //insert 00000000
		trie.insert(key1, key1); // insert 01000000
		trie.insert(key2, key2); // insert 00000010
		trie.reset();
		trie.delete(key2);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		//expect right child of root to be stub
		assertTrue(root.getRightChild().isStub());
		
		//expect left child of root to be interior
		Node left = root.getLeftChild();
		assertFalse(left.isStub());
		assertFalse(left.isLeaf());
		
		//assert
		Node prefix_00 = left.getLeftChild();
		assertTrue(prefix_00.isLeaf());
		assertFalse(prefix_00.isEmpty());
		assertArrayEquals(key0, prefix_00.getKey());
		Node prefix_01 = left.getRightChild();
		//01 should be stub
		assertTrue(prefix_01.isStub());
		
		
		
	}
	
	@Test
	public void testDeltaAfterDeleteRemoveChildLeafNoShrinking() {
		
		//001
		//000011
		//000010		
		
		//reset
		//delete 001
		
		Set<Byte> first = new HashSet<>();
		first.add(SIX);
		byte[] key0 = getByteArray(first); //01000000
		first.remove(SIX);
		first.add(THREE);
		byte[] key1 = getByteArray(first); //00001000
		first.add(TWO);
		byte[] key2 = getByteArray(first); //00001100
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key1); //insert 01000000
		trie.insert(key1, key1); //insert 00001000
		trie.insert(key2, key0); //insert 00001100
		trie.reset();
		trie.delete(key0); //insert 01000000
		
		System.out.println("TRIE\n"+ trie);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		System.out.println("DELTA\n" + delta);
		Node root = delta.getRoot();
		
		//00 should be stubby
		Node prefix_00 = (root.getLeftChild()).getLeftChild();
		assertTrue(prefix_00.isStub());
		//001 should be changed, empty leaf
		
		Node prefix_01 = (root.getLeftChild()).getRightChild();
		assertTrue(prefix_01.isLeaf());
		assertTrue(prefix_01.isEmpty());
		
		
	}
	
	
	@Test
	public void testDeltaAfterDeletingNonexistentKey() {
		
		//generate some trie, maybe full?
		//reset
		//try deleting key that doesn't exist
		//check that delta trie is completely stub
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //00000000
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //10000000
		first.add(SIX);
		byte[] key2 = getByteArray(first); //11000000
		first.remove(SEVEN);
		byte[] key3 = getByteArray(first); // 01000000
		first.add(FIVE);
		byte[] rogueKey = getByteArray(first);
		
		
		//initialize depth 2 full trie
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key0, rogueKey);
		trie.insert(key1, key0);
		trie.insert(key2, key3);
		trie.insert(key3, rogueKey);
		trie.reset();
		//trie.delete(rogueKey); //nothing should happen
		
		//expect delta to be completely stubby at root level
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		
		System.out.println("DELETING NONEXISTENT KEY");
		System.out.println(delta);
		
		
		Node root = delta.getRoot();
		Node left = root.getLeftChild();
		Node right = root.getRightChild();
		
		assertTrue(left.isStub());
		assertTrue(right.isStub());
		
		
	}
	
	
	/**
	 * Testing multiple changes since reset
	 */
	
	/*
	 * Testing strategies
	 * 
	 * Types of changes
	 * Insert:
	 * - change value of existing key (no structural changes)
	 * - split leaves
	 * 	- depth of split: 1, >1
	 * 
	 * - 
	 * 
	 * Delete:
	 * 
	 * 
	 * different subtree
	 * - insert into different
	 * - 
	 * 
	 * same subtree
	 * - 
	 * - 
	 */
	
	@Test
	public void testDeltaMultipleValueUpdatesDifferentSubtrees() {
		//in trie with:
		//  00
		//  11
		//  01
		//  10
				
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //00000000
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //10000000
		first.add(SIX);
		byte[] key2 = getByteArray(first); //11000000
		first.remove(SEVEN);
		byte[] key3 = getByteArray(first); // 01000000
		first.add(FIVE);
		
		
		//initialize depth 2 full trie
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key1);
		trie.insert(key1, key0);
		trie.insert(key2, key3);
		trie.insert(key3, key2);
		
		trie.reset();
		
		trie.insert(key0, key0);
		trie.insert(key2, key2);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		
		//expect key0 and key2 to be leaves, and key1 and key3 to be stubs
		
		Node path_00 = root.getLeftChild().getLeftChild();
		assertTrue(path_00.isLeaf());
		assertArrayEquals(path_00.getValue(), key0);
		
		Node path_01 = root.getLeftChild().getRightChild();
		assertTrue(path_01.isStub());
		

		Node path_10 = root.getRightChild().getLeftChild();
		assertTrue(path_10.isStub());
		
		Node path_11 = root.getRightChild().getRightChild();
		assertTrue(path_11.isLeaf());
		assertArrayEquals(path_11.getValue(), key2);
		
	}
	
	@Test
	public void testDeltaMultipleValueUpdatesSameSubtree() {
		//in trie with:
		//  00
		//  11
		//  01
		//  10
						
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //00000000
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //10000000
		first.add(SIX);
		byte[] key2 = getByteArray(first); //11000000
		first.remove(SEVEN);
		byte[] key3 = getByteArray(first); // 01000000
		first.add(FIVE);
		
		//initialize depth 2 full trie
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		trie.insert(key2, key0);
		trie.insert(key1, key1);
		trie.insert(key3, key3);
		trie.insert(key0, key2);
		trie.reset();
		
		trie.insert(key2, key3);
		trie.insert(key0, key1);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		Node path_00 = root.getLeftChild().getLeftChild();
		Node path_01 = root.getLeftChild().getRightChild();
		Node path_10 = root.getRightChild().getLeftChild();
		Node path_11 = root.getRightChild().getRightChild();
		
		assertFalse(root.getLeftChild().isStub());
		
		assertTrue(path_00.isLeaf());
		assertArrayEquals(key1, path_00.getValue());
		assertTrue(path_01.isStub());
		assertTrue((path_10).isStub());
		assertTrue((path_11).isLeaf());
		assertArrayEquals(key3, path_11.getValue());
	}
	
	@Test
	public void testMultipleSplitsDifferentBranch() {
		
		//0
		//1000
		//1010
		//110
		//reset
		//1001
		//1011
		
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //00000000
		first.add(SEVEN);
		byte[] key1 = getByteArray(first); //10000000
		first.add(SIX);
		byte[] key2 = getByteArray(first); //11000000
		first.remove(SIX);
		first.add(FIVE);
		byte[] key3 = getByteArray(first); // 10100000
		first.add(FOUR);
		byte[] key4 = getByteArray(first); //10110000
		first.remove(FIVE);
		byte[] key5 = getByteArray(first);
		
		//initialize depth 2 full trie
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key0);
		trie.insert(key1, key2);
		trie.insert(key2, key0);
		trie.insert(key3, key1);
		trie.reset();
		
		trie.insert(key4, key4);
		trie.insert(key5, key4);
		
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		
		Node path_0 = root.getLeftChild();
		assertTrue(path_0.isStub());
		Node path_1 = root.getRightChild();
		assertFalse(path_1.isStub());
		
		Node path_11 = path_1.getRightChild();
		assertTrue(path_11.isStub());
		
		Node path_10 = path_1.getLeftChild();
		assertFalse(path_10.isStub());
		
		Node path_100 = path_10.getLeftChild();
		Node path_101 = path_10.getRightChild();
		Node path_1000 = path_100.getLeftChild();
		Node path_1001 = path_100.getRightChild();
		Node path_1010 = path_101.getLeftChild();
		Node path_1011 = path_101.getRightChild();
		assertTrue(path_1000.isLeaf());
		assertTrue(path_1001.isLeaf());
		assertTrue(path_1010.isLeaf());
		assertTrue(path_1011.isLeaf());
		
	}
	
	@Test
	public void testMultipleSplitsSameBranch() {
		//00
		//01000
		//reset
		//0101
		//01001
		Set<Byte> first = new HashSet<>();
		byte[] key0 = getByteArray(first); //0
		first.add(SIX);
		byte[] key2 = getByteArray(first); //01000
		first.add(FOUR);
		byte[] key1 = getByteArray(first); //0101
		first.remove(FOUR);
		first.add(THREE);
		byte[] key3 = getByteArray(first); //01001
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		trie.insert(key0, key1); //0
		trie.insert(key1, key2); //0101
		trie.reset();
		trie.insert(key2, key0); //01000
		trie.insert(key3, key1); //01001
		MPTDeltaExposed delta = new MPTDeltaExposed(trie);
		Node root = delta.getRoot();
		
		Node path_00 = root.getLeftChild().getLeftChild();
		assertTrue(path_00.isStub());
		
		Node path_01 = root.getLeftChild().getRightChild();
		//check subtree rooted at path_01 has no stubs
		assertFalse(path_01.isStub());
		
		Node path_01001 = root.getLeftChild().getRightChild().getLeftChild().getLeftChild().getRightChild();
		Node path_01000 = root.getLeftChild().getRightChild().getLeftChild().getLeftChild().getLeftChild();
		Node path_0101 = root.getLeftChild().getRightChild().getLeftChild().getRightChild();
		
		assertTrue(path_01001.isLeaf());
		assertTrue(path_01000.isLeaf());
		assertTrue(path_0101.isLeaf());
		
		Node path_0100 = root.getLeftChild().getRightChild().getLeftChild().getLeftChild();
		Node path_010 = root.getLeftChild().getRightChild().getLeftChild();
		
		assertFalse(path_0100.isLeaf());
		assertFalse(path_0100.isStub());
		assertFalse(path_010.isLeaf());
		assertFalse(path_010.isStub());
		
		
	}
	
}
