package mpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import crpyto.CryptographicDigest;

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
		try {
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
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
		
		try {
			// check that the entries are in the tree
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("A".getBytes())));
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("B".getBytes())));
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("C".getBytes())));
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("D".getBytes())));
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("E".getBytes())));
			Assert.assertTrue(Arrays.equals("3".getBytes(), mpt.get("F".getBytes())));
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
		try {
			Assert.assertTrue(mpt.deleteKey("B".getBytes()));
			Assert.assertEquals(null, mpt.get("B".getBytes()));
			Assert.assertTrue(mpt.deleteKey("D".getBytes()));
			Assert.assertEquals(null, mpt.get("D".getBytes()));
			Assert.assertTrue(mpt.deleteKey("F".getBytes()));
			Assert.assertEquals(null, mpt.get("F".getBytes()));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
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
		try {
			for(int key = 0; key < numberOfEntries; key++) {
				String keyString = "key"+Integer.toString(key);
				String valueString = "value"+Integer.toString(key)+salt;
				// System.out.println("checking key: " + keyString.getBytes()+" ("+keyString+")");
				// System.out.println("should be value: "+valueString.getBytes()+" ("+valueString+")");
				byte[] valueBytes = mpt.get(keyString.getBytes());
				// System.out.println("value was: "+valueBytes);
				Assert.assertTrue(Arrays.equals(valueString.getBytes(), valueBytes));
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
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
		try {
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
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testTrieRepeatedInsertionsReturnFalse() {
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
		try {
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
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
	public void testPathSerialization() {
		int key = 100;
		String salt = "serialization";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(1000, salt);
		String keyString = "key"+Integer.toString(key);
		String valueString = "value"+Integer.toString(key)+salt;
		MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
		byte[] serialization = path.serialize();
		try {
			MerklePrefixTrie fromBytes = MerklePrefixTrie.deserialize(serialization);
			Assert.assertTrue("deserialized path contains the specific entry", 
					Arrays.equals(fromBytes.get(keyString.getBytes()), valueString.getBytes()));
			Assert.assertTrue("deserialized path commitment matches" ,
					Arrays.equals(fromBytes.getCommitment(), mpt.getCommitment()));
		} catch (InvalidMPTSerializationException | IncompleteMPTException e) {
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
		Assert.assertEquals("00000001", MerklePrefixTrie.byteArrayAsBitString(ONE));
		
		byte[] TWO = new byte[] { 2 };
		Assert.assertEquals("00000010", MerklePrefixTrie.byteArrayAsBitString(TWO));
		
		byte[] NINE = new byte[] { 9 };
		Assert.assertEquals("00001001", MerklePrefixTrie.byteArrayAsBitString(NINE));
		
		byte[] HUNDRED = new byte[] { 100 };
		Assert.assertEquals("01100100", MerklePrefixTrie.byteArrayAsBitString(HUNDRED));
		
		byte[] ONE_TWO = new byte[] {1, 2};
		Assert.assertEquals("00000001" + "00000010", MerklePrefixTrie.byteArrayAsBitString(ONE_TWO));
	}
	
	@Test
	public void testGetBitVaried() {
		byte[] ONE_TWO = new byte[] {1, 2};
		String binaryStr = "00000001" + "00000010";
		int[] expected = new int[16];
		for (int i = 0; i < 16; i++) {
			expected[i] = Character.getNumericValue(binaryStr.charAt(i));
		}
		for (int i = 0; i < 16; i++) {
			//System.out.println(MerklePrefixTrie.getBit(ONE_TWO, i));
			if (expected[i] == 1) {
				assertTrue(MerklePrefixTrie.getBit(ONE_TWO, i));
			} else if (expected[i] == 0) {
				assertFalse(MerklePrefixTrie.getBit(ONE_TWO, i));
			} else {
				fail("binary string contains non-binary values");
			}
		}
	}
	
	@Test
	public void testGetBitVariedManyBytes() {
		byte[] MANY_BYTES = new byte[] {9, 100, (byte) 200, 32};
		String binaryStr = "00001001" + "01100100" + "11001010" + "00100000";
		int[] expected = new int[32];
		for (int i = 0; i < 32; i++) {
			expected[i] = Character.getNumericValue(binaryStr.charAt(i));
		}
		for (int i = 0; i < 16; i++) {
			System.out.println(MerklePrefixTrie.getBit(MANY_BYTES, i));
			if (expected[i] == 1) {
				assertTrue(MerklePrefixTrie.getBit(MANY_BYTES, i));
				
			} else if (expected[i] == 0) {
				assertFalse(MerklePrefixTrie.getBit(MANY_BYTES, i));
				
			} else {
				fail("binary string contains non-binary values");
			}
		}
	}
	
	
	@Test
	public void testCopySinglePathDepth1() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		byte[] bytes = new byte[] {0};
		mpt.set(bytes, "1".getBytes());
		MerklePrefixTrie path = mpt.copyPath(bytes);
		try {
			Assert.assertTrue("expect path contains correct entry", Arrays.equals("1".getBytes(), path.get(bytes)));
			Assert.assertTrue("expect same commitment of trie and path", Arrays.equals(mpt.getCommitment(), path.getCommitment()));	
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCopyTwoPathsDepth1() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		byte[] first = new byte[] {0};
		byte[] second = new byte[] {1};
		mpt.set(first, "1".getBytes());
		MerklePrefixTrie path0 = mpt.copyPath(first);
		mpt.set(second, "2".getBytes());		
		path0 = mpt.copyPath(first);
		MerklePrefixTrie path1 = mpt.copyPath(second);
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
		
		try {
			Assert.assertArrayEquals("3".getBytes(), mpt.get(third));
			Assert.assertArrayEquals("1".getBytes(), mpt.get(first));
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetNewPrefixSingleLength() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		//insert 10
		byte[] first = new byte[] { 2 };
		System.out.println("SETTING FIRST");
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(first)));
		mpt.set(first, "1".getBytes());
		System.out.println(mpt);
		
		
		//insert 11
		byte[] second = new byte[] { 3 };
		System.out.println("SETTING SECOND");
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(second)));
		mpt.set(second, "2".getBytes());
		System.out.println(mpt);
		
		//insert 1001
		byte[] third = new byte[] { 9 };
		System.out.println("SETTING THIRD");
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(third)));
		mpt.set(third, "3".getBytes());
		System.out.println(mpt);
		
		
	}
	
	@Test
	public void testCopyPath() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		//insert 10
		byte[] first = new byte[] { 2 };
		byte[] second = new byte[] { 3 };
		byte[] third = new byte[] { 9 };
		mpt.set(first, "1".getBytes());
		//System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(second)));
		mpt.set(second, "2".getBytes());
		//System.out.println(mpt);
		//System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(third)));
		mpt.set(third, "3".getBytes());
		System.out.println(mpt);
		
		System.out.println("path to first: " + mpt.copyPath(first));
		System.out.println("path to second: " + mpt.copyPath(second));
		System.out.println("path to third: " + mpt.copyPath(third));
	}
	
	@Test
	public void testSetBranchFromExistingPrefix() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		//insert 1000
		byte[] first = new byte[] { 8 };
		System.out.println("SETTING FIRST");
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(first)));
		mpt.set(first, "1".getBytes());
		System.out.println(mpt);
		
		//insert 1010001
		byte[] second = new byte[] { 81 };
		System.out.println("SETTING SECOND");
		System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(second)));
		mpt.set(second, "10".getBytes());
		System.out.println(mpt);
	}
	
	@Test
	public void testSetExtendExistingPrefix() {
		//insert 101011
		
		
		//insert 101011110
		
		
	}
	
	@Test
	public void testCopyPathKeyPresent() {
		int n = 1000;
		String salt = "path test";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(1000, salt);
		for(int key = 0; key < n; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
			try {
				byte[] value = path.get(keyString.getBytes());
				Assert.assertTrue("path should contain correct (key,value)", Arrays.equals(valueString.getBytes(), value));
			}catch(Exception e) {
				Assert.fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testCopyPathKeyNotPresent() {
		int n = 1000;
		String salt = "path test";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(1000, salt);
		for(int offset = 1; offset < 1000; offset++) {
			// not in tree
			int key = n+offset;
			String keyString = "key"+Integer.toString(key);
			// copy path here should map a path to an empty leaf or a leaf
			// with a different key - so when we call get on the path it 
			// it returns nulls
			MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
			try {
				byte[] value = path.get(keyString.getBytes());
				Assert.assertTrue("not in tree - path should map to empty leaf", value == null);
			}catch(Exception e) {
				Assert.fail(e.getMessage());
			}	
		}
	}
	
	
}
