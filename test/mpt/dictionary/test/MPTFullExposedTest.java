package mpt.dictionary.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import mpt.core.Node;
import mpt.dictionary.MPTDictionaryFull;

/**
 * Class to test the internal structure of MPTDictionaryFull
 *  
 * @author christinalee
 *
 */

public class MPTFullExposedTest {
	
	private class MPTFullExposed extends MPTDictionaryFull {
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
	 * Testing Strategy
	 * 
	 * insert
	 * 	 inserted key direct child of root: yes, no
	 *   insert requires split: yes, no
	 *   length of shared prefix b/w two keys after splitting point: 0, 1, >1
	 *   insertion results in which child: right, left
	 *   # times key is inserted: 0, 1, >1
	 *   structure changed b/w inserts for same key: yes, no
	 *   
	 *   
	 * delete
	 * 	 key to delete in trie: yes, no
	 *   length of trie shrinking: 0, 1, >1
	 * 	 
	 */
	
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
	
	
	
	/*
	 * Testing insert
	 */
	
	//# times key is inserted: 0
	@Test
	public void testEmpty() {
		MPTFullExposed trie = new MPTFullExposed();
		Node root = trie.getRoot();
		
		Node right = root.getLeftChild();
		assertTrue(right.isEmpty() && right.isLeaf());
		Node left = root.getRightChild();
		assertTrue(left.isEmpty() && left.isLeaf());
	}
	
	
	//inserted key is direct child of root: yes
	//insert requires split: no
	//insert results in which child: right
	//# times key is inserted: 1
	@Test
	public void testInsertRightChildOfRoot() {
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> firstByte = new HashSet<>();
		firstByte.add(SEVEN);
		byte[] key1 = getByteArray(firstByte);
		byte[] val1 = getByteArray(firstByte);
		
		trie.insert(key1, val1);
		
		//check trie structure
		Node root = trie.getRoot();
		Node right = root.getRightChild();
		assertFalse(right.isEmpty());
		assertTrue(right.isLeaf());
		assertArrayEquals(right.getKey(), key1);
		
		Node left = root.getLeftChild();
		assertTrue(left.isEmpty() && left.isLeaf());
	}
	
	@Test
	public void testInsertLeftChildOfRoot() {
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> firstByte = new HashSet<>(); //01000001
		firstByte.add(SIX);
		firstByte.add(ZERO);
		byte[] key1 = getByteArray(firstByte);
		byte[] val1 = getByteArray(firstByte);
		
		trie.insert(key1, val1);
		
		//System.out.println(trie);
		
		//check trie structure
		Node root = trie.getRoot();
		Node left = root.getLeftChild();
		assertFalse(left.isEmpty());
		assertTrue(left.isLeaf());
		assertArrayEquals(left.getKey(), key1);
		
		
		Node right = root.getRightChild();
		assertTrue(right.isEmpty() && right.isLeaf());
	}
	
	@Test
	public void testInsertRightChildOfNonrootReplace() {
		
		//in trie start with 00000000, 00000001
		//then insert 00010000
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>();
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(ZERO);
		byte[] key2 = getByteArray(second);
		Set<Byte> third = new HashSet<>();
		third.add(FOUR);
		byte[] key3 = getByteArray(third);
		
		trie.insert(key1	, key1);
		trie.insert(key2, key2);
		
		//check whether long split for shared prefix is correct
		Node root = trie.getRoot();
		Node right;
		Node left = root;
		
		for (int i = 0; i < 7; i++) {
			right = left.getRightChild();
			assertTrue(right.isLeaf());
			assertTrue(right.isEmpty());
			
			left = left.getLeftChild();
			assertTrue(!left.isLeaf());
			
		}
		
		right = left.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key2, right.getKey());
		left = left.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key1, left.getKey());
		
		
		//try adding key that replaces empty leaf (NO SPLIT)
		trie.insert(key3, key3);
		
		root = trie.getRoot();
		left = root;
		
		for (int i = 0; i < 3; i++) {
			right = left.getRightChild();
			assertTrue(right.isLeaf());
			assertTrue(right.isEmpty());
			
			left = left.getLeftChild();
			assertTrue(!left.isLeaf());
		}
		
		right = left.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key3, right.getKey());
		
		left = left.getLeftChild();
		assertTrue(!left.isLeaf());
		
		for (int i = 0; i < 3; i++) {
			right = left.getRightChild();
			assertTrue(right.isLeaf());
			assertTrue(right.isEmpty());
			
			left = left.getLeftChild();
			assertTrue(!left.isLeaf());
		}
		
		right = left.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key2, right.getKey());
		left = left.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key1, left.getKey());
		
		System.out.println("LOOK HERE");
		System.out.println(trie);
	}
	
	@Test
	public void testInsertLeftChildrenOfNonrootReplace() {
		
		//in trie start with 11111111 and 11111110
		//then insert 11000000
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>();
		first.add(ZERO);
		first.add(ONE);
		first.add(TWO);
		first.add(THREE);
		first.add(FOUR);
		first.add(FIVE);
		first.add(SIX);
		first.add(SEVEN);
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(ONE);
		second.add(TWO);
		second.add(THREE);
		second.add(FOUR);
		second.add(FIVE);
		second.add(SIX);
		second.add(SEVEN);
		byte[] key2 = getByteArray(second);
		Set<Byte> third = new HashSet<>();
		third.add(SEVEN);
		third.add(SIX);
		byte[] key3 = getByteArray(third);
		
		trie.insert(key1	, key1);
		trie.insert(key2, key2);
		
		System.out.println("LOOK HERE NOW");
		System.out.println(trie);
		
		//check whether long split for shared prefix is correct
		Node root = trie.getRoot();
		Node left;
		Node right = root;
		
		for (int i = 0; i < 7; i++) {
			left = right.getLeftChild();
			assertTrue(left.isLeaf());
			assertTrue(left.isEmpty());
			
			right = right.getRightChild();
			assertTrue(!right.isLeaf());
			
		}
		
		left = right.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key2, left.getKey());
		right = right.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key1, right.getKey());
		
		
		//try adding key that replaces empty leaf (NO SPLIT)
		trie.insert(key3, key3);
		
		root = trie.getRoot();
		right = root;
		
		for (int i = 0; i < 2; i++) {
			left = right.getLeftChild();
			assertTrue(left.isLeaf());
			assertTrue(left.isEmpty());
			
			right = right.getRightChild();
			assertTrue(!right.isLeaf());
		}
		
		left = right.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key3, left.getKey());
		
		right = right.getRightChild();
		assertTrue(!right.isLeaf());
		
		for (int i = 0; i < 4; i++) {
			left = right.getLeftChild();
			assertTrue(left.isLeaf());
			assertTrue(left.isEmpty());
			
			right = right.getRightChild();
			assertTrue(!right.isLeaf());
		}
		
		left = right.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key2, left.getKey());
		right = right.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key1, right.getKey());
		
		
	}
	
	@Test
	public void testInsertRightLongSplit() {
		
		//insert 0010000, then insert 00000010
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> firstByte = new HashSet<>(); //00100000
		firstByte.add(FIVE);
		byte[] key1 = getByteArray(firstByte);
		byte[] val1 = getByteArray(firstByte);
		
		trie.insert(key1, val1);
		
		Set<Byte> byte2 = new HashSet<>();
		byte2.add(ONE);
		byte[] key2 = getByteArray(byte2);
		byte[] val2 = getByteArray(byte2);
		
		trie.insert(key2, val2);
		
		Node root = trie.getRoot();
		
		Node right = root.getRightChild();
		assertTrue(right.isEmpty() && right.isLeaf());
		Node left = root.getLeftChild();
		assertFalse(left.isLeaf());
		
		Node index_2_0 = left.getLeftChild();
		assertFalse(index_2_0.isLeaf());
		Node index_2_1 = left.getRightChild();
		assertTrue(index_2_1.isLeaf() && index_2_1.isEmpty());
		
		Node index_3_0 = index_2_0.getLeftChild();
		assertTrue(!index_3_0.isEmpty() && index_3_0.isLeaf());
		assertArrayEquals(key2, index_3_0.getKey());
		Node index_3_1 = index_2_0.getRightChild();
		assertTrue(!index_3_1.isEmpty() && index_3_0.isLeaf());
		assertArrayEquals(key1, index_3_1.getKey());
		
	}
	
	@Test
	public void testInsertZigZagSplit() {
		//SETUP
		//insert 00001000
		//insert 10101001
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>(); //00010000
		first.add(THREE);
		byte[] key1 = getByteArray(first);
		
		Set<Byte> second = new HashSet<>();
		second.add(SEVEN);
		second.add(FIVE);
		second.add(THREE);
		second.add(ZERO);
		byte[] key2 = getByteArray(second);
		
		//TEST
		//insert 10101000
		Set<Byte> third = new HashSet<>();
		third.add(SEVEN);
		third.add(FIVE);
		third.add(THREE); //split should happen here
		byte[] key3 = getByteArray(third);
		
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		trie.insert(key3, key3);
		
		Node root = trie.getRoot();
		
		Node index_1_0 = root.getLeftChild();
		assertTrue(index_1_0.isLeaf());
		assertFalse(index_1_0.isEmpty());
		assertArrayEquals(key1, index_1_0.getKey());
		
		Node left = root;
		Node right;
		Node emptyRightLeaf;
		
		//check 1010
		for (int i = 0; i < 2; i++) {
			right = left.getRightChild();
			assertFalse(right.isLeaf());
			
			emptyRightLeaf = right.getRightChild();
			assertTrue(emptyRightLeaf.isEmpty());
			assertTrue(emptyRightLeaf.isLeaf());
			
			left = right.getLeftChild();
			assertFalse(left.isLeaf());
			
			Node emptyLeftLeaf = left.getLeftChild();
			assertTrue(emptyLeftLeaf.isEmpty());
			assertTrue(emptyLeftLeaf.isLeaf());
		}
		
		//check 1001 and 1000
		Node prefix_1 = left.getRightChild();
		assertFalse(prefix_1.isLeaf());
		
		emptyRightLeaf = prefix_1.getRightChild();
		assertTrue(emptyRightLeaf.isLeaf());
		assertTrue(emptyRightLeaf.isEmpty());
		
		Node prefix_10 = prefix_1.getLeftChild();
		assertFalse(prefix_10.isLeaf());
		
		emptyRightLeaf = prefix_10.getRightChild();
		assertTrue(emptyRightLeaf.isLeaf());
		assertTrue(emptyRightLeaf.isEmpty());
		
		Node prefix_100 = prefix_10.getLeftChild();
		assertFalse(prefix_100.isLeaf());
		
		Node prefix_1000 = prefix_100.getLeftChild();
		assertTrue(prefix_1000.isLeaf());
		assertFalse(prefix_1000.isEmpty());
		assertArrayEquals(key3, prefix_1000.getKey());
		
		Node prefix_1001 = prefix_100.getRightChild();
		assertTrue(prefix_1001.isLeaf());
		assertFalse(prefix_1001.isEmpty());
		assertArrayEquals(key2, prefix_1001.getKey());
		
		
	}
	
	/*
	 * Testing delete
	 */
	
	
	@Test
	public void testDeleteRightChildOfRoot() {
		//insert 0
		//insert 10000010
		//delete 10000000
		
		//check that right child is still there
		
		//delete 10000010
		
		//check that right child is empty
		
		//insert 00000000
		//insert 10000000
		//delete 10000001 (should be no change)
		//delete 10000000
				
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> firstByte = new HashSet<>(); //00100000
		byte[] key1 = getByteArray(firstByte);
		Set<Byte> second = new HashSet<>();
		second.add(SEVEN);
		byte[] key2 = getByteArray(second);
		Set<Byte> third = new HashSet<>();
		third.add(SEVEN);
		third.add(ZERO);
		byte[] key3 = getByteArray(third);
		
		//initialize
		trie.insert(key1, key1);
		trie.insert(key2, key2);
		
		trie.delete(key3);
		//check that structure hasn't changed
		Node root = trie.getRoot();
		Node left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key1, left.getKey());
		
		Node right = root.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key2, right.getKey());
		
		
		trie.delete(key2);
		//check that deletion happened
		root = trie.getRoot();
		left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key1, left.getKey());
		
		right = root.getRightChild();
		assertTrue(right.isLeaf());
		assertTrue(right.isEmpty());
		
	}
	
	
	
	
	@Test
	public void testDeleteShrinkLongLeft() {
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>(); //00100000
		first.add(FIVE);
		byte[] key1 = getByteArray(first);
		Set<Byte> second = new HashSet<>();
		second.add(ZERO);
		byte[] key2 = getByteArray(second);
		
		//insert 00100000
		trie.insert(key1, key2);
		//insert 00000001
		trie.insert(key2, key1);
		
		//delete 00100000
		trie.delete(key1);
		
		//check that trie shrinks to just root and a left child of 0 and empty right child
		Node root = trie.getRoot();
		Node left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key2, left.getKey());
		
		Node right = root.getRightChild();
		assertTrue(right.isLeaf());
		assertTrue(right.isEmpty());
		
	}
	
	@Test
	public void testDeleteShrinkLongRight() {
		
		Set<Byte> sum = new HashSet<>();
		sum.add(SEVEN);
		sum.add(SIX);
		sum.add(FIVE);
		sum.add(FOUR);
		sum.add(THREE);
		sum.add(TWO);
		sum.add(ONE);
		byte[] key1 = getByteArray(sum);
		sum.add(ZERO);
		byte[] key2 = getByteArray(sum);
		
		MPTFullExposed trie = new MPTFullExposed();
		//insert 11111111
		trie.insert(key2, key1);
		
		//insert 11111110
		trie.insert(key1, key2);
		
		//delete 11111111
		trie.delete(key2);
		
		//check that trie shrinks to just root and empty right child and left child of 1
		Node root = trie.getRoot();
		Node left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertTrue(left.isEmpty());
		
		Node right = root.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key1, right.getKey());
		
	}
	
	@Test
	public void testDeleteShrinkLongZigZag() {
		
		MPTFullExposed trie = new MPTFullExposed();
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
		//remove 11101011
		trie.delete(key2);
		
		//check that trie shrinks to just root and left child of 0 and right child of 1 with
		//key 11101010
		Node root = trie.getRoot();
		Node left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertFalse(left.isEmpty());
		assertArrayEquals(key0, left.getKey());
		
		Node right = root.getRightChild();
		assertTrue(right.isLeaf());
		assertFalse(right.isEmpty());
		assertArrayEquals(key1, right.getKey());
		
	}
	
	
	/*
	 * Testing mix of inserts and deletes
	 */
	
	@Test
	public void testReinsertAfterDelete() {
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>(); //11100000
		first.add(SEVEN);
		first.add(SIX);
		byte[] key0 = getByteArray(first); //11000000
		
		first.add(FIVE);
		byte[] key1 = getByteArray(first); //11100000
		
		//insert 110
		trie.insert(key0, key1);
		//insert 111
		trie.insert(key1, key0);
		//delete 110
		trie.delete(key0);
		//insert 110
		trie.insert(key0, key0);
		
		
		//check that left and right children is nonempty leaf
		Node root = trie.getRoot();
		
		Node left = root.getLeftChild();
		assertTrue(left.isLeaf());
		assertTrue(left.isEmpty());
		
		//prefix 1
		Node right = root.getRightChild();
		assertFalse(right.isLeaf());
		
		left = right.getLeftChild();
		assertTrue(left.isLeaf());
		assertTrue(left.isEmpty());
		
		left = right.getLeftChild();
		assertTrue(left.isEmpty());
		assertTrue(left.isLeaf());
		
		//prefix 11
		right = right.getRightChild();
		assertFalse(right.isLeaf());
		
		
		//leaf 110
		Node prefix_110 = right.getLeftChild();
		assertTrue(prefix_110.isLeaf());
		assertFalse(prefix_110.isEmpty());
		assertArrayEquals(key0, prefix_110.getKey());
		
		//leaf 111
		Node prefix_111 = right.getRightChild();
		assertTrue(prefix_111.isLeaf());
		assertFalse(prefix_111.isEmpty());
		assertArrayEquals(key1, prefix_111.getKey());
	}
	
	
	@Test
	public void testInsertSplitsDeletedEmptyLeafUnshrunk() {
		
		MPTFullExposed trie = new MPTFullExposed();
		Set<Byte> first = new HashSet<>(); //11100000
		first.add(SIX);
		byte[] key0 = getByteArray(first); //11000000
		first.add(FOUR);
		byte[] key1 = getByteArray(first);
		
		first.remove(FOUR);
		first.add(FIVE);
		byte[] key2 = getByteArray(first);
		
		first.add(THREE);
		byte[] key3 = getByteArray(first);
		
		first.add(TWO);
		byte[] key4 = getByteArray(first);
		
		//insert 0101 key1
		trie.insert(key1, key0);
		//insert 0100 key0
		trie.insert(key0, key1);
		//insert 011
		trie.insert(key2, key0);
		
		//delete 011
		trie.delete(key2);
		
		//insert 011011
		trie.insert(key3, key4);
		
		//insert 011010
		trie.insert(key4, key2);
		
		//check that split happened
		Node root = trie.getRoot();
		
		//on path
		Node index_1_0 = root.getLeftChild();
		assertFalse(index_1_0.isLeaf());
		
		Node index_1_1 = root.getRightChild();
		assertTrue(index_1_1.isLeaf());
		assertTrue(index_1_1.isEmpty());
		
		Node index_2_0 = index_1_0.getLeftChild();
		assertTrue(index_2_0.isLeaf());
		assertTrue(index_2_0.isEmpty());
		
		//on path
		Node index_2_1 = index_1_0.getRightChild();
		assertFalse(index_2_1.isLeaf());
		
		Node index_3_2 = index_2_1.getLeftChild();
		
		Node index_4_4 = index_3_2.getLeftChild(); //prefix 0100
		assertTrue(index_4_4.isLeaf());
		assertFalse(index_4_4.isEmpty());
		assertArrayEquals(key0, index_4_4.getKey());
		
		Node index_4_5 = index_3_2.getRightChild(); //prefix 0101
		assertTrue(index_4_5.isLeaf());
		assertFalse(index_4_5.isEmpty());
		assertArrayEquals(key1, index_4_5.getKey());
		
		
		//on path
		Node index_3_3 = index_2_1.getRightChild();
		assertFalse(index_3_3.isLeaf());
		
		Node index_4_6 = index_3_3.getLeftChild();
		assertFalse(index_4_6.isLeaf());
		
		Node index_5_13 = index_4_6.getRightChild();
		assertFalse(index_5_13.isLeaf());
		
		Node prefix_011010 = index_5_13.getLeftChild();
		assertTrue(prefix_011010.isLeaf());
		assertFalse(prefix_011010.isEmpty());
		assertArrayEquals(key3, prefix_011010.getKey());
		
		Node prefix_011011 = index_5_13.getRightChild();
		assertTrue(prefix_011011.isLeaf());
		assertFalse(prefix_011011.isEmpty());
		assertArrayEquals(key4, prefix_011011.getKey());
		
	}
	
	@Test
	public void testInsertResplitIntoDeletedTrieShrunk() {
		//insert 0101
		//insert 01001
		//insert 011
		
		//delete 01001
		//insert 01000
		
		
	}
	
	@Test
	public void testInsertSplitIntoDeletedTrieShrunk() {
		//insert 0101
		//insert 01001
		//insert 011
		
		//delete 011
		//insert 01110
		//insert 01111
		
	}
	
	
}
