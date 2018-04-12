package mpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MerklePrefixTrieTest {
	
	public static MerklePrefixTrie makeMerklePrefixTrie(int numberOfEntries, String salt) {
		return MerklePrefixTrieTest.makeMerklePrefixTrie(
				MerklePrefixTrieTest.getKeyValuePairs(numberOfEntries, salt));
	}
	
	public static List<Map.Entry<String, String>> getKeyValuePairs(int numberOfEntries, String salt){
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>();
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			list.add(Map.entry(keyString, valueString));
		}
		return list;
	}
	
	public static MerklePrefixTrie makeMerklePrefixTrie(List<Map.Entry<String, String>> kvpairs) {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		for(Map.Entry<String, String> kvpair : kvpairs) {
			mpt.set(kvpair.getKey().getBytes(), kvpair.getValue().getBytes());
		}
		return mpt;
	}
	
	
	@Test 
	public void testTrieInsertionsManyOrdersProduceTheSameTrie() {
		int numberOfKeys = 1000;
		int numberOfShuffles = 10;
		String salt = "test";
		List<Map.Entry<String, String>> kvpairs = MerklePrefixTrieTest.getKeyValuePairs(numberOfKeys, salt);
		MerklePrefixTrie mptBase = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs);
		byte[] commitment = mptBase.getCommitment();
		for(int iteration = 0; iteration <  numberOfShuffles; iteration++) {
			Collections.shuffle(kvpairs);
			MerklePrefixTrie mpt2 = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs);
			byte[] commitment2 = mpt2.getCommitment();
			Assert.assertTrue(Arrays.equals(commitment, commitment2));
		}
	}
	
	@Test
	public void testTrieInsertionsBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		// insert the entries
		Assert.assertTrue(mpt.set("A".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt.set("B".getBytes(), "2".getBytes()));
		Assert.assertTrue(mpt.set("C".getBytes(), "3".getBytes()));
		Assert.assertTrue(mpt.set("D".getBytes(), "3".getBytes()));		
		Assert.assertTrue(mpt.set("E".getBytes(), "2".getBytes()));		
		Assert.assertTrue(mpt.set("F".getBytes(), "1".getBytes()));
		
		byte[] commitment1 = mpt.getCommitment();
		
		
		// insert them again - should not change tree since already inserted
		Assert.assertFalse(mpt.set("A".getBytes(), "1".getBytes()));
		Assert.assertFalse(mpt.set("B".getBytes(), "2".getBytes()));
		Assert.assertFalse(mpt.set("C".getBytes(), "3".getBytes()));
		Assert.assertFalse(mpt.set("D".getBytes(), "3".getBytes()));		
		Assert.assertFalse(mpt.set("E".getBytes(), "2".getBytes()));		
		Assert.assertFalse(mpt.set("F".getBytes(), "1".getBytes()));
		
		//check commitment is same after reinsertions that don't change trie structure
		Assert.assertTrue(Arrays.equals(commitment1, mpt.getCommitment()));
		
		// check that the entries are in the tree
		Assert.assertTrue(Arrays.equals("1".getBytes(), mpt.get("A".getBytes())));
		Assert.assertTrue(Arrays.equals("2".getBytes(), mpt.get("B".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("C".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("D".getBytes())));
		Assert.assertTrue(Arrays.equals("2".getBytes(), mpt.get("E".getBytes())));
		Assert.assertTrue(Arrays.equals("1".getBytes(), mpt.get("F".getBytes())));
		
		// check that other keys are not 
		Assert.assertEquals(null, mpt.get("G".getBytes()));		
		Assert.assertEquals(null, mpt.get("H".getBytes()));		
		Assert.assertEquals(null, mpt.get("I".getBytes()));		
		Assert.assertEquals(null, mpt.get("J".getBytes()));		
		Assert.assertEquals(null, mpt.get("K".getBytes()));		
	}
	
	@Test
	public void testTrieInsertionsUpdateValue() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();

		// insert the entries
		Assert.assertTrue(mpt.set("A".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt.set("B".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt.set("C".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt.set("D".getBytes(), "1".getBytes()));		
		Assert.assertTrue(mpt.set("E".getBytes(), "1".getBytes()));		
		Assert.assertTrue(mpt.set("F".getBytes(), "1".getBytes()));
		
		// update the value
		Assert.assertTrue(mpt.set("A".getBytes(), "2".getBytes()));
		Assert.assertTrue(mpt.set("B".getBytes(), "2".getBytes()));
		Assert.assertTrue(mpt.set("C".getBytes(), "2".getBytes()));
		Assert.assertTrue(mpt.set("D".getBytes(), "2".getBytes()));		
		Assert.assertTrue(mpt.set("E".getBytes(), "2".getBytes()));		
		Assert.assertTrue(mpt.set("F".getBytes(), "2".getBytes()));
		
		// update the value
		Assert.assertTrue(mpt.set("A".getBytes(), "3".getBytes()));
		Assert.assertTrue(mpt.set("B".getBytes(), "3".getBytes()));
		Assert.assertTrue(mpt.set("C".getBytes(), "3".getBytes()));
		Assert.assertTrue(mpt.set("D".getBytes(), "3".getBytes()));		
		Assert.assertTrue(mpt.set("E".getBytes(), "3".getBytes()));		
		Assert.assertTrue(mpt.set("F".getBytes(), "3".getBytes()));
		
		// check that the entries are in the tree
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("A".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("B".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("C".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("D".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("E".getBytes())));
		Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("F".getBytes())));
	}
	
	@Test
	public void testTrieDeleteBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		// insert the entries
		Assert.assertTrue(mpt.set("A".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt.set("B".getBytes(), "2".getBytes()));
		Assert.assertTrue(mpt.set("C".getBytes(), "3".getBytes()));
		Assert.assertTrue(mpt.set("D".getBytes(), "3".getBytes()));		
		Assert.assertTrue(mpt.set("E".getBytes(), "2".getBytes()));		
		Assert.assertTrue(mpt.set("F".getBytes(), "1".getBytes()));
		
		//try removing entries that were never in trie
		
		assertFalse(mpt.deleteKey("G".getBytes()));
		assertFalse(mpt.deleteKey("H".getBytes()));
		assertFalse(mpt.deleteKey("I".getBytes()));
	
		// remove them 
		Assert.assertTrue(mpt.deleteKey("B".getBytes()));
		Assert.assertEquals(null, mpt.get("B".getBytes()));
		Assert.assertTrue(mpt.deleteKey("D".getBytes()));
		Assert.assertEquals(null, mpt.get("D".getBytes()));
		Assert.assertTrue(mpt.deleteKey("F".getBytes()));
		Assert.assertEquals(null, mpt.get("F".getBytes()));
		
		// try re-removing entries that were already removed from trie
		assertFalse(mpt.deleteKey("B".getBytes()));
		assertFalse(mpt.deleteKey("D".getBytes()));
		assertFalse(mpt.deleteKey("F".getBytes()));
		
		// make a tree with the same entries, added in a different order
		MerklePrefixTrie mpt2 = new MerklePrefixTrie();
		Assert.assertTrue(mpt2.set("E".getBytes(), "2".getBytes()));	
		Assert.assertTrue(mpt2.set("A".getBytes(), "1".getBytes()));
		Assert.assertTrue(mpt2.set("C".getBytes(), "3".getBytes()));
		
		// this tree should be the same 
		Assert.assertTrue(Arrays.equals(mpt.getCommitment(), mpt2.getCommitment()));
	}
	
	
	@Test
	public void testTrieInsertionsGet() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(numberOfEntries, salt);
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			// System.out.println("checking key: " + keyString.getBytes()+" ("+keyString+")");
			// System.out.println("should be value: "+valueString.getBytes()+" ("+valueString+")");
			byte[] valueBytes = mpt.get(keyString.getBytes());
			// System.out.println("value was: "+valueBytes);
			Assert.assertTrue(Arrays.equals(valueString.getBytes(), valueBytes));
		}
	}
	
	@Test 
	public void testTrieMultipleInsertionsGetMostRecentValue() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(numberOfEntries, salt);
		salt = "modified";
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			// update values
			Assert.assertTrue(mpt.set(keyString.getBytes(), valueString.getBytes()));
		}
		// now make sure the next get returns the updated values
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			// System.out.println("checking key: " + keyString.getBytes()+" ("+keyString+")");
			// System.out.println("should be value: "+valueString.getBytes()+" ("+valueString+")");
			byte[] valueBytes = mpt.get(keyString.getBytes());
			// System.out.println("value was: "+valueBytes);
			Assert.assertTrue(Arrays.equals(valueString.getBytes(), valueBytes));
		}
	}
	
	@Test
	public void testTrieInsertionsAgainReturnFalse() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(numberOfEntries, salt);
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			// trying to insert again should return false
			Assert.assertFalse(mpt.set(keyString.getBytes(), valueString.getBytes()));
		}
	}
	
	@Test
	public void testTrieDeletions() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(numberOfEntries, salt);
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			Assert.assertTrue(Arrays.equals(mpt.get(keyString.getBytes()), valueString.getBytes()));
			if(key > 499) {
				Assert.assertTrue(mpt.deleteKey(keyString.getBytes()));				
			}
		}
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			if (key > 499) {
				Assert.assertEquals(null, mpt.get(keyString.getBytes()));			
			}else {
				Assert.assertTrue(Arrays.equals(mpt.get(keyString.getBytes()), valueString.getBytes()));
			}
		}
		MerklePrefixTrie mpt2 = MerklePrefixTrieTest.makeMerklePrefixTrie(500, salt);
		Assert.assertTrue(Arrays.equals(mpt.getCommitment(), mpt2.getCommitment()));
	}
	
	@Test
	public void testTrieSerializationFullTrie() {
		List<Map.Entry<String, String>> kvpairs = MerklePrefixTrieTest.getKeyValuePairs(1000, "test");
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs);
		byte[] asbytes = mpt.serialize();
		try {
			MerklePrefixTrie mptFromBytes = MerklePrefixTrie.deserialize(asbytes);
			Assert.assertTrue(mptFromBytes.equals(mpt));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testEqualityBasic() {
		List<Map.Entry<String, String>> kvpairs = MerklePrefixTrieTest.getKeyValuePairs(1000, "test");
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs);
		MerklePrefixTrie mpt2 = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs);
		MerklePrefixTrie mpt3 = MerklePrefixTrieTest.makeMerklePrefixTrie(kvpairs.subList(0, 500));
		Assert.assertTrue(mpt.equals(mpt2));
		Assert.assertFalse(mpt.equals(mpt3));
	}
	
	@Test
	public void testEqualitySymmetric() {
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(1000, "some salt changes the hash");
		Assert.assertTrue(mpt.equals(mpt));
	}
	
	@Test 
	public void testByteAtBitString() {
		byte[] bs = new byte[]{(byte) 0xff, (byte) 0xff};
		Assert.assertEquals(MerklePrefixTrie.byteArrayAsBitString(bs), "1111111111111111");
	}
	
	@Test
	public void testGetBit() {
		// for all zeros all bits should be zero
		byte[] ALL_ZEROS = new byte[]{0, 0, 0, 0};
		for(int i = 0; i < 32; i++) {
			Assert.assertFalse(MerklePrefixTrie.getBit(ALL_ZEROS, i));
		}
		// for all ones all bits should be one
		byte[] ALL_ONES = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}; 
		for(int i = 0; i < 32; i++) {
			Assert.assertTrue(MerklePrefixTrie.getBit(ALL_ONES, i));
		}

	}
	
	@Test
	public void testByteArrayAsBitString() {
		byte[] ONE = new byte[] { 1	};
	}
	
	@Test
	public void testCopySinglePathDepth1() {
		
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		byte[] bytes = new byte[] {0};
		mpt.set(bytes, "1".getBytes());
		//System.out.println(MerklePrefixTrie.byteArrayAsHexString("1".getBytes()));
		//System.out.println(mpt);
		//System.out.println(mpt.getCommitment());
		byte[] first = mpt.getCommitment();
		
		//System.out.println(mpt);
		byte[] second = mpt.getCommitment();
		
		Assert.assertTrue(Arrays.equals(first, second));
		
		//System.out.println(mpt);
		
		
		MerklePrefixTrie path = mpt.copyPath(bytes);
		//System.out.println(path);
		//System.out.println(path.getCommitment());
		//System.out.println(mpt);
		
		Assert.assertTrue("expect same commitment of trie and path", Arrays.equals(mpt.getCommitment(), path.getCommitment()));
		
	}
	
	@Test
	public void testCopyTwoPathsDepth1() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		byte[] first = new byte[] {0};
		byte[] second = new byte[] {1};
		mpt.set(first, "1".getBytes());
		System.out.println(mpt);
		System.out.println("after first insert: " + mpt);
		MerklePrefixTrie path0 = mpt.copyPath(first);
		System.out.println("path 0: " + path0);
		mpt.set(second, "2".getBytes());
		
		System.out.println("after second insert: " + mpt);
		
		path0 = mpt.copyPath(first);
		MerklePrefixTrie path1 = mpt.copyPath(second);
		
		System.out.println("path 0: " + path0);
		System.out.println("path 1: " + path1);
		
		Assert.assertArrayEquals(path0.getCommitment(), path1.getCommitment());
	}
	
	@Test
	public void testSetSplitLeaf() {
		
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		byte[] first = new byte[] {2}; //path = 10
		
		mpt.set(first, "1".getBytes());
		System.out.println("input 1: " + MerklePrefixTrie.byteArrayAsBitString(first));
		//System.out.println(mpt);
		
		//splitting leaves
		byte[] second = new byte[] {3}; //path = 11
		mpt.set(second,  "2".getBytes());
		System.out.println("input 2: " + MerklePrefixTrie.byteArrayAsBitString(second));
		//System.out.println(mpt);
		
		byte[] third = new byte[] {9}; //path = 1001
		System.out.println("input: " + MerklePrefixTrie.byteArrayAsBitString(third));
		mpt.set(third, "3".getBytes());
		System.out.println(mpt);
		
		Assert.assertArrayEquals("3".getBytes(), mpt.get(third));
		Assert.assertArrayEquals("1".getBytes(), mpt.get(first));
	}
	
	@Test
	public void testSetNewPrefixSingleLength() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		//insert 10
		byte[] first = new byte[] {2 };
		
		//insert 0
	}
	
	@Test
	public void testSetNewPrefixLongerLength() {
		//insert 1000
		
		//insert 101
	}
	
	
	
	
}
