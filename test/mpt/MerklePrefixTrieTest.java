package mpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import crpyto.CryptographicDigest;

public class MerklePrefixTrieTest {
	
	@Test 
	public void testTrieInsertionsManyOrdersProduceTheSameTrie() {
		int numberOfKeys = 1000;
		int numberOfShuffles = 10;
		String salt = "test";
		List<Map.Entry<String, String>> kvpairs = Utils.getKeyValuePairs(numberOfKeys, salt);
		MerklePrefixTrie mptBase = Utils.makeMerklePrefixTrie(kvpairs);
		byte[] commitment = mptBase.commitment();
		for(int iteration = 0; iteration <  numberOfShuffles; iteration++) {
			Collections.shuffle(kvpairs);
			MerklePrefixTrie mpt2 = Utils.makeMerklePrefixTrie(kvpairs);
			byte[] commitment2 = mpt2.commitment();
			Assert.assertTrue(Arrays.equals(commitment, commitment2));
		}
	}
		
	@Test
	public void testMPTInsertionBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();

		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());
		
		// check that they were added
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
	public void testMPTInsertionBasicMultipleUpdatesGetMostRecentValue() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "1".getBytes());
		mpt.insert("C".getBytes(), "1".getBytes());
		mpt.insert("D".getBytes(), "1".getBytes());		
		mpt.insert("E".getBytes(), "1".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());
		
		// update the value
		mpt.insert("A".getBytes(), "2".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "2".getBytes());
		mpt.insert("D".getBytes(), "2".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "2".getBytes());
		
		// update the value
		mpt.insert("A".getBytes(), "3".getBytes());
		mpt.insert("B".getBytes(), "3".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "3".getBytes());		
		mpt.insert("F".getBytes(), "3".getBytes());
		
		// check that the entries are in the tree
		Arrays.equals("3".getBytes(), mpt.get("A".getBytes()));
		Arrays.equals("3".getBytes(), mpt.get("B".getBytes()));
		Arrays.equals("3".getBytes(), mpt.get("C".getBytes()));
		Arrays.equals("3".getBytes(), mpt.get("D".getBytes()));
		Arrays.equals("3".getBytes(), mpt.get("E".getBytes()));
		Arrays.equals("3".getBytes(), mpt.get("F".getBytes()));	
	}
	
	@Test
	public void testTrieDeleteBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());
	
		// delete entries that were never in trie
		mpt.delete("G".getBytes());
		mpt.delete("H".getBytes());
		mpt.delete("I".getBytes());

		// delete actual entries
		mpt.delete("B".getBytes());
		Assert.assertEquals(null, mpt.get("B".getBytes()));
		mpt.delete("D".getBytes());
		Assert.assertEquals(null, mpt.get("D".getBytes()));
		mpt.delete("F".getBytes());
		Assert.assertEquals(null, mpt.get("F".getBytes()));
		
		// make a tree with the same entries, added in a different order
		MerklePrefixTrie mpt2 = new MerklePrefixTrie();
		mpt2.insert("E".getBytes(), "2".getBytes());	
		mpt2.insert("A".getBytes(), "1".getBytes());
		mpt2.insert("C".getBytes(), "3".getBytes());
		
		// this tree should be the same 
		Assert.assertTrue(Arrays.equals(mpt.commitment(), mpt2.commitment()));
	}
	
	
	@Test
	public void testTrieInsertionsGet() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(numberOfEntries, salt);
		try {
			for(int key = 0; key < numberOfEntries; key++) {
				String keyString = "key"+Integer.toString(key);
				String valueString = "value"+Integer.toString(key)+salt;
				 System.out.println("checking key: " + keyString.getBytes()+" ("+keyString+")");
				 System.out.println("should be value: "+valueString.getBytes()+" ("+valueString+")");
				byte[] valueBytes = mpt.get(keyString.getBytes());
				 System.out.println("value was: "+valueBytes);
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
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(numberOfEntries, salt);
		salt = "modified";
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			// update values
			mpt.insert(keyString.getBytes(), valueString.getBytes());
		}
		
		// now make sure the next get returns the updated values
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			 System.out.println("checking key: " + keyString.getBytes()+" ("+keyString+")");
			 System.out.println("should be value: "+valueString.getBytes()+" ("+valueString+")");
			byte[] valueBytes = mpt.get(keyString.getBytes());
			 System.out.println("value was: "+valueBytes);
			Assert.assertTrue(Arrays.equals(valueString.getBytes(), valueBytes));
		}
	}
	
	
	@Test
	public void testTrieDeletions() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(numberOfEntries, salt);
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			Assert.assertTrue(Arrays.equals(mpt.get(keyString.getBytes()), valueString.getBytes()));
			if(key > 499) {
				mpt.delete(keyString.getBytes());				
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
		MerklePrefixTrie mpt2 = Utils.makeMerklePrefixTrie(500, salt);
		Assert.assertTrue(Arrays.equals(mpt.commitment(), mpt2.commitment()));
	}
	
	@Test
	public void testTrieSerializationFullTrie() {
		List<Map.Entry<String, String>> kvpairs = Utils.getKeyValuePairs(1000, "test");
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(kvpairs);
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
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(1000, salt);
		String keyString = "key"+Integer.toString(key);
		String valueString = "value"+Integer.toString(key)+salt;
		MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
		byte[] serialization = path.serialize();
		try {
			MerklePrefixTrie fromBytes = MerklePrefixTrie.deserialize(serialization);
			Assert.assertTrue("deserialized path contains the specific entry", 
					Arrays.equals(fromBytes.get(keyString.getBytes()), valueString.getBytes()));
			Assert.assertTrue("deserialized path commitment matches" ,
					Arrays.equals(fromBytes.commitment(), mpt.commitment()));
		} catch (InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testEqualityBasic() {
		List<Map.Entry<String, String>> kvpairs = Utils.getKeyValuePairs(1000, "test");
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(kvpairs);
		MerklePrefixTrie mpt2 = Utils.makeMerklePrefixTrie(kvpairs);
		MerklePrefixTrie mpt3 = Utils.makeMerklePrefixTrie(kvpairs.subList(0, 500));
		Assert.assertTrue(mpt.equals(mpt2));
		Assert.assertFalse(mpt.equals(mpt3));
	}
	
	@Test
	public void testEqualitySymmetric() {
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(1000, "some salt changes the hash");
		Assert.assertTrue(mpt.equals(mpt));
	}
	
	@Test 
	public void testByteAtBitString() {
		byte[] bs = new byte[]{(byte) 0xff, (byte) 0xff};
		Assert.assertEquals(Utils.byteArrayAsBitString(bs), "1111111111111111");
	}
	
	@Test
	public void testGetBit() {
		// for all zeros all bits should be zero
		byte[] ALL_ZEROS = new byte[]{0, 0, 0, 0};
		for(int i = 0; i < 32; i++) {
			Assert.assertFalse(Utils.getBit(ALL_ZEROS, i));
		}
		// for all ones all bits should be one
		byte[] ALL_ONES = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}; 
		for(int i = 0; i < 32; i++) {
			Assert.assertTrue(Utils.getBit(ALL_ONES, i));
		}

	}
	
	@Test
	public void testByteArrayAsBitString() {
		byte[] ONE = new byte[] { 1	};
		Assert.assertEquals("00000001", Utils.byteArrayAsBitString(ONE));
		
		byte[] TWO = new byte[] { 2 };
		Assert.assertEquals("00000010", Utils.byteArrayAsBitString(TWO));
		
		byte[] NINE = new byte[] { 9 };
		Assert.assertEquals("00001001", Utils.byteArrayAsBitString(NINE));
		
		byte[] HUNDRED = new byte[] { 100 };
		Assert.assertEquals("01100100", Utils.byteArrayAsBitString(HUNDRED));
		
		byte[] ONE_TWO = new byte[] {1, 2};
		Assert.assertEquals("00000001" + "00000010", Utils.byteArrayAsBitString(ONE_TWO));
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
				assertTrue(Utils.getBit(ONE_TWO, i));
			} else if (expected[i] == 0) {
				assertFalse(Utils.getBit(ONE_TWO, i));
			} else {
				fail("binary string contains non-binary values");
			}
		}
	}
	
	@Test 
	public void testGetPrefixOverlap() {
		byte[] a = new byte[] {9, 100};
		byte[] b = new byte[] {9, 1};
		// 0000100101100100
		// 0000100100000001
		// 0123456789	<- overlap is 9 [includes first differing index]
		Assert.assertEquals(9, Utils.getOverlappingPrefix(a, b));
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
			System.out.println(Utils.getBit(MANY_BYTES, i));
			if (expected[i] == 1) {
				assertTrue(Utils.getBit(MANY_BYTES, i));
				
			} else if (expected[i] == 0) {
				assertFalse(Utils.getBit(MANY_BYTES, i));
				
			} else {
				fail("binary string contains non-binary values");
			}
		}
	}
	
	@Test
	public void testCopySinglePathDepth1() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		byte[] bytes = new byte[] {0};
		try {
			mpt.insert(bytes, "1".getBytes());
			MerklePrefixTrie path = mpt.copyPath(bytes);
			Assert.assertTrue("expect path contains correct entry", Arrays.equals("1".getBytes(), path.get(bytes)));
			Assert.assertTrue("expect same commitment of trie and path", Arrays.equals(mpt.commitment(), path.commitment()));	
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCopyTwoPathsDepth1() {
		try {
			MerklePrefixTrie mpt = new MerklePrefixTrie();
			byte[] first = new byte[] {0};
			byte[] second = new byte[] {1};
			mpt.insert(first, "1".getBytes());
			MerklePrefixTrie path0 = mpt.copyPath(first);
			mpt.insert(second, "2".getBytes());		
			path0 = mpt.copyPath(first);
			MerklePrefixTrie path1 = mpt.copyPath(second);
			Assert.assertArrayEquals(path0.commitment(), path1.commitment());
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetSplitLeaf() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		byte[] first = new byte[] {2}; //path = 10
		
		mpt.insert(first, "1".getBytes());
		System.out.println("input 1: " + Utils.byteArrayAsBitString(first));
		//System.out.println(mpt);
		
		//splitting leaves
		byte[] second = new byte[] {3}; //path = 11
		mpt.insert(second,  "2".getBytes());
		System.out.println("input 2: " + Utils.byteArrayAsBitString(second));
		//System.out.println(mpt);
		
		byte[] third = new byte[] {9}; //path = 1001
		System.out.println("input: " + Utils.byteArrayAsBitString(third));
		mpt.insert(third, "3".getBytes());
		System.out.println(mpt);
		
		Assert.assertArrayEquals("3".getBytes(), mpt.get(third));
		Assert.assertArrayEquals("1".getBytes(), mpt.get(first));
	}
	
	@Test
	public void testSetNewPrefixSingleLength() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		//insert 10
		byte[] first = new byte[] { 2 };
		System.out.println("SETTING FIRST");
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest(first)));
		mpt.insert(first, "1".getBytes());
		System.out.println(mpt);
		
		
		//insert 11
		byte[] second = new byte[] { 3 };
		System.out.println("SETTING SECOND");
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest(second)));
		mpt.insert(second, "2".getBytes());
		System.out.println(mpt);
		
		//insert 1001
		byte[] third = new byte[] { 9 };
		System.out.println("SETTING THIRD");
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest(third)));
		mpt.insert(third, "3".getBytes());
		System.out.println(mpt);		
	}
	
	@Test
	public void testCopyPath() {
		try {
			MerklePrefixTrie mpt = new MerklePrefixTrie();
			
			//insert 10
			byte[] first = new byte[] { 2 };
			byte[] second = new byte[] { 3 };
			byte[] third = new byte[] { 9 };
			mpt.insert(first, "1".getBytes());
			//System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(second)));
			mpt.insert(second, "2".getBytes());
			//System.out.println(mpt);
			//System.out.println(MerklePrefixTrie.byteArrayAsBitString(CryptographicDigest.digest(third)));
			mpt.insert(third, "3".getBytes());
			System.out.println(mpt);
			
			System.out.println("path to first: " + mpt.copyPath(first));
			System.out.println("path to second: " + mpt.copyPath(second));
			System.out.println("path to third: " + mpt.copyPath(third));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetBranchFromExistingPrefix() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		//insert 1000
		byte[] first = new byte[] { 8 };
		System.out.println("SETTING FIRST");
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest(first)));
		mpt.insert(first, "1".getBytes());
		System.out.println(mpt);
		
		//insert 1010001
		byte[] second = new byte[] { 81 };
		System.out.println("SETTING SECOND");
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest(second)));
		mpt.insert(second, "10".getBytes());
		System.out.println(mpt);
	}
	
	@Test
	public void testCopyPathKeyPresent() {
		int n = 1000;
		String salt = "path test";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(1000, salt);
		for(int key = 0; key < n; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
			byte[] value = path.get(keyString.getBytes());
			Assert.assertTrue("path should contain correct (key,value)", Arrays.equals(valueString.getBytes(), value));
		}
	}
	
	@Test
	public void testCopyPathKeyNotPresent() {
		int n = 1000;
		String salt = "path test";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(1000, salt);
		for(int offset = 1; offset < 1000; offset++) {
			// not in tree
			int key = n+offset;
			String keyString = "key"+Integer.toString(key);
			// copy path here should map a path to an empty leaf or a leaf
			// with a different key - so when we call get on the path it 
			// it returns nulls
			MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
			byte[] value = path.get(keyString.getBytes());
			Assert.assertTrue("not in tree - path should map to empty leaf", value == null);	
		}
	}

	@Test
	public void testMPTBasicUpdateSequenceChangeValues() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		
		// mark everything as unchanged
		mpt.reset();

		// copy a path to a key
		byte[] key = "F".getBytes();
		MerklePrefixTrie path = mpt.copyPath(key);
		
		// change value
		mpt.insert("C".getBytes(), "100".getBytes());
		mpt.insert("D".getBytes(), "101".getBytes());		
		mpt.insert("E".getBytes(), "102".getBytes());		
		
		// calculate a new path to a key
		MerklePrefixTrie pathNew = mpt.copyPath(key);
		try {
			// save the changes 
			MerklePrefixTrieDelta changes = new MerklePrefixTrieDelta(mpt);
			
			// use the changes to calculate an update for the original path
			byte[] updates = changes.getUpdates(key);
			// update the original path
			path.deserializeUpdates(updates);
			
			// should produce the new path
			Assert.assertTrue(pathNew.equals(path));
		} catch (InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTBasicUpdateSequenceInsertValues() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		
		// mark everything as unchanged
		mpt.reset();

		// copy a path to a key
		byte[] key = "F".getBytes();
		MerklePrefixTrie path = mpt.copyPath(key);
		
		// change values
		mpt.insert("G".getBytes(), "100".getBytes());
		mpt.insert("H".getBytes(), "101".getBytes());
		mpt.insert("I".getBytes(), "102".getBytes());
		mpt.insert("J".getBytes(), "103".getBytes());
		
		// calculate a new path to a key
		MerklePrefixTrie pathNew = mpt.copyPath(key);
		
		// save the changes 
		MerklePrefixTrieDelta changes = new MerklePrefixTrieDelta(mpt);
		try {
			// use the changes to calculate an update for the original path
			byte[] updates = changes.getUpdates(key);
			// update the original path
			path.deserializeUpdates(updates);
			
			// should produce the new path
			Assert.assertTrue(pathNew.equals(path));
		} catch (InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTBasicUpdateSequenceDeleteValues() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		
		// mark everything as unchanged
		mpt.reset();

		// copy a path to a key
		byte[] key = "F".getBytes();
		MerklePrefixTrie path = mpt.copyPath(key);
		
		System.out.println("\noriginal:\n"+mpt);
		System.out.println("\npath original:\n"+path);
		
		// delete values
		mpt.delete("A".getBytes());
		mpt.delete("B".getBytes());
		mpt.delete("C".getBytes());
		mpt.delete("D".getBytes());

		// calculate a new path to a key
		MerklePrefixTrie pathNew = mpt.copyPath(key);
		
		System.out.println("\nnew:\n"+mpt);
		System.out.println("\npath new:\n"+pathNew);
		
		// save the changes 
		MerklePrefixTrieDelta changes = new MerklePrefixTrieDelta(mpt);
		
		System.out.println("\nchanges:\n"+changes);
		try {
			// use the changes to calculate an update for the original path
			byte[] updates = changes.getUpdates(key);
			// update the original path
			path.deserializeUpdates(updates);
			
			System.out.println("\nafter updates:\n"+path);

			// should produce the new path
			Assert.assertTrue(pathNew.equals(path));
		} catch (InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testMPTBasicUpdateSequence() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		
		// mark everything as unchanged
		mpt.reset();
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest("F".getBytes())));
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest("C".getBytes())));
		System.out.println(Utils.byteArrayAsBitString(CryptographicDigest.digest("G".getBytes())));

		// copy a path to a key
		byte[] key = "F".getBytes();
		MerklePrefixTrie path = mpt.copyPath(key);
		System.out.println("\noriginal:\n"+mpt);
		System.out.println("\npath original:\n"+path);
		
		// make some changes
		mpt.insert("C".getBytes(), "100".getBytes());
		mpt.insert("G".getBytes(), "1".getBytes());
		
		// calculate a new path to a key
		MerklePrefixTrie pathNew = mpt.copyPath(key);
		System.out.println("\nnew:\n"+mpt);
		System.out.println("\npath new:\n"+pathNew);
		
		// save the changes 
		MerklePrefixTrieDelta changes = new MerklePrefixTrieDelta(mpt);
		System.out.println("\nchanges:\n"+changes);
		try {
			// use the changes to calculate an update for the original path
			byte[] updates = changes.getUpdates(key);
			// update the original path
			path.deserializeUpdates(updates);
			System.out.println("\nafter updates:\n"+path);
			
			// should produce the new path
			Assert.assertTrue(pathNew.equals(path));
		} catch (InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testCreatePartialTrieBasic() {
			MerklePrefixTrie mpt = new MerklePrefixTrie();

		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		

		System.out.println("\noriginal:\n"+mpt);

		// create a partial tree
		MerklePrefixTriePartial partialmpt = new  MerklePrefixTriePartial(mpt);
		System.out.println("\npartial:\n"+partialmpt);
		
		// add a path
		byte[] key = "F".getBytes();
		partialmpt.addPath(mpt, key);
		try {
			Assert.assertTrue(Arrays.equals("1".getBytes(), partialmpt.get(key)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));
			// copy a path to a key
			System.out.println("\npartial with path:\n"+partialmpt);
			
		} catch (IncompleteMPTException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testCreatePartialTrieMultiplePaths() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();

		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		

		System.out.println("\noriginal:\n"+mpt);

		// create a partial tree
		MerklePrefixTriePartial partialmpt = new  MerklePrefixTriePartial(mpt);
		System.out.println("\npartial:\n"+partialmpt);
		
		// add a path
		byte[] key1 = "E".getBytes();
		byte[] key2 = "F".getBytes();
		partialmpt.addPath(mpt, key1);
		System.out.println("\npartial with path 1:\n"+partialmpt);
		partialmpt.addPath(mpt, key2);
		System.out.println("\npartial with path 2:\n"+partialmpt);
		try {
			Assert.assertTrue(Arrays.equals("2".getBytes(), partialmpt.get(key1)));
			Assert.assertTrue(Arrays.equals("1".getBytes(), partialmpt.get(key2)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));
		} catch (IncompleteMPTException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	
}
