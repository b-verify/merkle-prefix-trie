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

import com.google.protobuf.ByteString;

import crpyto.CryptographicDigest;
import serialization.MptSerialization;

public class MerklePrefixTrieTest {
	
	@Test 
	public void testTrieInsertionsManyOrdersProduceTheSameTrie() {
		int numberOfKeys = 1000;
		int numberOfShuffles = 10;
		String salt = "test";
		List<Map.Entry<String, String>> kvpairs = Utils.getKeyValuePairs(numberOfKeys, salt);
		MerklePrefixTrie mptBase = Utils.makeMerklePrefixTrie(kvpairs);
		byte[] commitment = mptBase.getCommitment();
		for(int iteration = 0; iteration <  numberOfShuffles; iteration++) {
			Collections.shuffle(kvpairs);
			MerklePrefixTrie mpt2 = Utils.makeMerklePrefixTrie(kvpairs);
			byte[] commitment2 = mpt2.getCommitment();
			Assert.assertTrue(Arrays.equals(commitment, commitment2));
		}
	}
		
	@Test
	public void testMPTInsertionBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		try {
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
			
		} catch (IncompleteMPTException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTInsertionBasicMultipleUpdatesGetMostRecentValue() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		try {
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
			
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testTrieDeleteBasic() {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		
		// insert the entries
		try {
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
			Assert.assertTrue(Arrays.equals(mpt.getCommitment(), mpt2.getCommitment()));
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
		try {
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
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	
	@Test
	public void testTrieDeletions() {
		int numberOfEntries = 1000;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(numberOfEntries, salt);
		try {
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
			Assert.assertTrue(Arrays.equals(mpt.getCommitment(), mpt2.getCommitment()));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
					Arrays.equals(fromBytes.getCommitment(), mpt.getCommitment()));
		} catch (InvalidMPTSerializationException | IncompleteMPTException e) {
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
			Assert.assertTrue("expect same commitment of trie and path", Arrays.equals(mpt.getCommitment(), path.getCommitment()));	
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
			Assert.assertArrayEquals(path0.getCommitment(), path1.getCommitment());
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetSplitLeaf() {
		try {
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
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetNewPrefixSingleLength() {
		try {
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
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
		
		
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
		try {
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
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
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
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(1000, salt);
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
	
	@Test
	public void testUpdateSingleStub() {
		int n = 20;
		int key = 8;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(n, salt);
		// take a path in the MPT	
		// +0101101
		String keyString = "key"+Integer.toString(key);
		MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
		
		// update a different key which will update a single stub 
		// alters stub +00
		String keyStringToUpdate = "key"+Integer.toString(5);
		byte[] updateHash = CryptographicDigest.digest(keyStringToUpdate.getBytes());
		int prefixIdx = 1;
				
		try {
			mpt.insert(keyStringToUpdate.getBytes(), "OTHER VALUE".getBytes());
			
			// get a new copy of the path
			MerklePrefixTrie pathUpdated = mpt.copyPath(keyString.getBytes());
			
			// should have different commitments
			Assert.assertFalse(Arrays.equals(path.getCommitment(), pathUpdated.getCommitment()));
			
			// find the updated stub
			Node stub = pathUpdated.getNodeAtPrefix(updateHash,prefixIdx);
			byte[] updateBytes = 
					MptSerialization.MerklePrefixTrieUpdate.newBuilder()
					.addUpdates(MptSerialization.Update.newBuilder()
							.setFullPath(ByteString.copyFrom(updateHash))
							.setIndex(prefixIdx)
							.setNode(stub.serialize()))
					.build().toByteArray();
			// update the stub 
			path.deserializeUpdates(updateBytes);
			Node stub2 = path.getNodeAtPrefix(updateHash,prefixIdx);
			// check that the stub was updated
			Assert.assertTrue(stub.equals(stub2));
			// check that the commitments now match
			Assert.assertTrue(Arrays.equals(path.getCommitment(), pathUpdated.getCommitment()));
		} catch (IncompleteMPTException | InvalidMPTSerializationException e) {
			Assert.fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testUpdateSingleStubWrongLocationGivesError() {
		int n = 20;
		int key = 8;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(n, salt);
		// take a path in the MPT		
		String keyString = "key"+Integer.toString(key);
		byte[] pathHash = CryptographicDigest.digest(keyString.getBytes());
		MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());
		try {
			byte[] updateBytes = 
					MptSerialization.MerklePrefixTrieUpdate.newBuilder()
					.addUpdates(MptSerialization.Update.newBuilder()
							.setFullPath(ByteString.copyFrom(pathHash))
							.setIndex(15) // this prefix will not be in the MPT
							.setNode(MptSerialization.Node.newBuilder()
									.setStub(MptSerialization.Stub.newBuilder()
												.setHash(ByteString.copyFrom("test".getBytes())))))
					.build().toByteArray();
			// update the stub 
			path.deserializeUpdates(updateBytes);
			Assert.fail("should throw exception");
		} catch (InvalidMPTSerializationException e) {
			
		}
		
	}
	
	@Test
	public void testUpdateMultipleStubs() {
		int n = 50;
		int key = 8;
		String salt = "";
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(n, salt);
		// take a path in the MPT		
		// +0101101
		String keyString = "key"+Integer.toString(key);
		byte[] keyStringHash = CryptographicDigest.digest(keyString.getBytes());
		MerklePrefixTrie path = mpt.copyPath(keyString.getBytes());

		// alters stub +1
		String keyUpdate1 = "key"+Integer.toString(1);
		byte[] keyUpdate1Hash = CryptographicDigest.digest(keyUpdate1.getBytes());
		int prefix1 = 0;
		Assert.assertEquals(prefix1, Utils.getOverlappingPrefix(keyUpdate1Hash, keyStringHash));

		// alters stub +00
		String keyUpdate2 = "key"+Integer.toString(5);
		byte[] keyUpdate2Hash = CryptographicDigest.digest(keyUpdate2.getBytes());
		int prefix2 = 1;
		Assert.assertEquals(prefix2, Utils.getOverlappingPrefix(keyUpdate2Hash, keyStringHash));

		// alters stub +0100
		String keyUpdate3 = "key"+Integer.toString(13);
		byte[] keyUpdate3Hash = CryptographicDigest.digest(keyUpdate3.getBytes());
		int prefix3 = 3;
		Assert.assertEquals(3, Utils.getOverlappingPrefix(keyUpdate3Hash, keyStringHash));

		// alters stub +011
		String keyUpdate4 = "key"+Integer.toString(15);
		byte[] keyUpdate4Hash = CryptographicDigest.digest(keyUpdate4.getBytes());
		int prefix4 = 2;
		Assert.assertEquals(prefix4, Utils.getOverlappingPrefix(keyUpdate4Hash, keyStringHash));
		

		try {
			// update the MPT
			mpt.insert(keyUpdate1.getBytes(), "some value".getBytes());
			mpt.insert(keyUpdate2.getBytes(), "some value".getBytes());
			mpt.insert(keyUpdate3.getBytes(), "some value".getBytes());
			mpt.insert(keyUpdate4.getBytes(), "some value".getBytes());
			
			MerklePrefixTrie path2 = mpt.copyPath(keyString.getBytes());
			System.out.println("PATH 1: ");
			System.out.println(path);
			System.out.println("\n\nPATH 2: ");
			System.out.println(path2);
			
			// now create the updates 
			Node stub1 = path2.getNodeAtPrefix(keyUpdate1Hash, prefix1);
			MptSerialization.Update update1 = MptSerialization.Update.newBuilder()
					.setFullPath(ByteString.copyFrom(keyUpdate1Hash))
					.setIndex(prefix1)
					.setNode(stub1.serialize())
					.build();
			System.out.println("get node at +"+Utils.byteArrayPrefixAsBitString(keyUpdate1Hash, prefix1));
			System.out.println(stub1);
			
			Node stub2 = path2.getNodeAtPrefix(keyUpdate2Hash, prefix2);
			MptSerialization.Update update2 = MptSerialization.Update.newBuilder()
					.setFullPath(ByteString.copyFrom(keyUpdate2Hash))
					.setIndex(prefix2)
					.setNode(stub2.serialize())
					.build();
			System.out.println("get node at +"+Utils.byteArrayPrefixAsBitString(keyUpdate2Hash, prefix2));
			System.out.println(stub2);

			
			Node stub3 = path2.getNodeAtPrefix(keyUpdate3Hash, prefix3);
			MptSerialization.Update update3 = MptSerialization.Update.newBuilder()
					.setFullPath(ByteString.copyFrom(keyUpdate3Hash))
					.setIndex(prefix3)
					.setNode(stub3.serialize())
					.build();
			System.out.println("get node at +"+Utils.byteArrayPrefixAsBitString(keyUpdate3Hash, prefix3));
			System.out.println(stub3);

			Node stub4 = path2.getNodeAtPrefix(keyUpdate4Hash, prefix4);
			MptSerialization.Update update4 = MptSerialization.Update.newBuilder()
					.setFullPath(ByteString.copyFrom(keyUpdate4Hash))
					.setIndex(prefix4)
					.setNode(stub4.serialize())
					.build();
			System.out.println("get node at +"+Utils.byteArrayPrefixAsBitString(keyUpdate4Hash, prefix4));
			System.out.println(stub4);

			
			MptSerialization.MerklePrefixTrieUpdate updates = MptSerialization.MerklePrefixTrieUpdate.newBuilder()
					.addUpdates(update1)
					.addUpdates(update2)
					.addUpdates(update3)
					.addUpdates(update4)
					.build();
			
			byte[] asbyte= updates.toByteArray();
			// add the updates to the path
			path.deserializeUpdates(asbyte);
			// check that the stubs are updated
			Node stub1New = path.getNodeAtPrefix(keyUpdate1Hash, prefix1);
			Node stub2New = path.getNodeAtPrefix(keyUpdate2Hash, prefix2);
			Node stub3New = path.getNodeAtPrefix(keyUpdate3Hash, prefix3);
			Node stub4New = path.getNodeAtPrefix(keyUpdate4Hash, prefix4);
					
			Assert.assertTrue(stub1.equals(stub1New));
			Assert.assertTrue(stub2.equals(stub2New));
			Assert.assertTrue(stub3.equals(stub3New));
			Assert.assertTrue(stub4.equals(stub4New));
			
			// check that the commitments match
			Assert.assertTrue(Arrays.equals(path.getCommitment(), path2.getCommitment()));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
