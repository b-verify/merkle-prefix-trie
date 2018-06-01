package mpt.dictionary.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import crypto.CryptographicDigest;
import mpt.core.InvalidSerializationException;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;
import serialization.generated.MptSerialization.MerklePrefixTrie;

public class MPTDictionaryFullTest {
	
	@Test
	public void testCommitmentCaluclationParallelized() {
		int numberOfKeys = 100000;
		String salt = "test";
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(numberOfKeys, salt);
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		MPTDictionaryFull mptCopy = Utils.makeMPTDictionaryFull(kvpairs);

		long start;
		long end;
		
		start = System.currentTimeMillis();
		byte[] regularCommitment = mptCopy.commitment();
		end = System.currentTimeMillis();
		System.out.println("----------> regular commitment time elapsed: "+(end-start));
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		start = System.currentTimeMillis();
		byte[] parallelizedCommitmentResult = mpt.commitmentParallelized(executorService);
		end = System.currentTimeMillis();
		System.out.println("----------> parallelized commitment time elapsed: "+(end-start));
		
		Assert.assertTrue(Arrays.equals(regularCommitment, parallelizedCommitmentResult));
		
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		}
	}
	
	@Test 
	public void testTrieInsertionsManyOrdersProduceTheSameTrie() {
		int numberOfKeys = 1000;
		int numberOfShuffles = 10;
		String salt = "test";
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(numberOfKeys, salt);
		MPTDictionaryFull mptBase = Utils.makeMPTDictionaryFull(kvpairs);
		byte[] commitment = mptBase.commitment();
		for(int iteration = 0; iteration <  numberOfShuffles; iteration++) {
			Collections.shuffle(kvpairs);
			MPTDictionaryFull mpt2 = Utils.makeMPTDictionaryFull(kvpairs);
			Assert.assertEquals(numberOfKeys, mpt2.size());
			byte[] commitment2 = mpt2.commitment();
			Assert.assertTrue(Arrays.equals(commitment, commitment2));
		}
	}
		
	@Test
	public void testMPTInsertionBasic() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// check that they were added
		Assert.assertTrue(Arrays.equals(value1, mpt.get(keyA)));
		Assert.assertTrue(Arrays.equals(value2, mpt.get(keyB)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyC)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyD)));
		Assert.assertTrue(Arrays.equals(value2, mpt.get(keyE)));
		Assert.assertTrue(Arrays.equals(value1, mpt.get(keyF)));
		
		// check that other keys are not 
		Assert.assertEquals(null, mpt.get(CryptographicDigest.hash("G".getBytes())));	
		Assert.assertEquals(null, mpt.get(CryptographicDigest.hash("H".getBytes())));		
		Assert.assertEquals(null, mpt.get(CryptographicDigest.hash("I".getBytes())));		
		Assert.assertEquals(null, mpt.get(CryptographicDigest.hash("J".getBytes())));			
		
	}
	
	@Test
	public void testMPTNodeCount() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// should have 11 nodes total 
		// 6 non-empty leaf nodes, 5 interior nodes, 0 empty leaf nodes
		Assert.assertEquals(11, mpt.countNodes());
		Assert.assertEquals(6, mpt.countNonEmptyLeafNodes());
		Assert.assertEquals(0, mpt.countEmptyLeafNodes());
		Assert.assertEquals(5, mpt.countInteriorNodes());
	}
	
	@Test
	public void testMPTHashToCommitCount() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// at start no hashes are calculated 
		// so committing should require calculating a hash 
		// for every node in the MPT
		Assert.assertEquals(11, mpt.countHashesRequiredToCommit());
		mpt.commitment();
		
		// update a single entry
		mpt.insert(keyF, value2);
		
		// now only the hashes on the path 
		// to keyF need to be recalculated
		Assert.assertEquals(4, mpt.countHashesRequiredToCommit());
		mpt.commitment();

		// if make no changes, shouldn't require any
		// recalculation of hashes
		Assert.assertEquals(0, mpt.countHashesRequiredToCommit());
	}
		
	@Test
	public void testMPTInsertionBasicMultipleUpdatesGetMostRecentValue() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value1);
		mpt.insert(keyC, value1);
		mpt.insert(keyD, value1);
		mpt.insert(keyE, value1);
		mpt.insert(keyF, value1);
		
		// update the value
		mpt.insert(keyA, value2);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value2);
		mpt.insert(keyD, value2);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value2);
		
		// update the value
		mpt.insert(keyA, value3);
		mpt.insert(keyB, value3);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value3);
		mpt.insert(keyF, value3);
		
		// check that the entries are in the tree
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyA)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyB)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyC)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyD)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyE)));
		Assert.assertTrue(Arrays.equals(value3, mpt.get(keyF)));

	}
	
	@Test
	public void testTrieDeleteBasic() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
	
		// delete entries that were never in trie
		mpt.delete(CryptographicDigest.hash("G".getBytes()));
		mpt.delete(CryptographicDigest.hash("H".getBytes()));
		mpt.delete(CryptographicDigest.hash("I".getBytes()));

		// delete actual entries
		mpt.delete(keyB);
		Assert.assertEquals(null, mpt.get(keyB));
		mpt.delete(keyD);
		Assert.assertEquals(null, mpt.get(keyD));
		mpt.delete(keyF);
		Assert.assertEquals(null, mpt.get(keyF));
		
		// make a tree with the same entries, added in a different order
		MPTDictionaryFull mpt2 = new MPTDictionaryFull();
		mpt2.insert(keyE, value2);	
		mpt2.insert(keyA, value1);
		mpt2.insert(keyC, value3);
		
		// this tree should be the same 
		Assert.assertTrue(Arrays.equals(mpt.commitment(), mpt2.commitment()));
	}
	
	
	@Test
	public void testTrieInsertionsGet() {
		int numberOfKeys = 1000;
		String salt = "get test";
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(numberOfKeys, salt);
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		try {
			for(Map.Entry<byte[], byte[]> kvpair : kvpairs) {
				byte[] value = mpt.get(kvpair.getKey());
				Assert.assertTrue(Arrays.equals(kvpair.getValue(), value));
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test 
	public void testTrieMultipleInsertionsGetMostRecentValue() {
		int numberOfEntries = 1000;
		String salt = "";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(numberOfEntries, salt);
		salt = "modified";
		for(int i = 0; i < numberOfEntries; i++) {
			byte[] key = Utils.getKey(i);
			byte[] modifiedValue = Utils.getValue(i, salt);
			// update values
			mpt.insert(key, modifiedValue);
		}
		
		// now make sure the next get returns the updated values
		for(int i = 0; i < numberOfEntries; i++) {
			byte[] key = Utils.getKey(i);
			byte[] modifiedValue = Utils.getValue(i, salt);
			Assert.assertTrue(Arrays.equals(modifiedValue, mpt.get(key)));
		}
	}
	
	@Test
	public void testTrieDeletions() {
		int numberOfEntries = 1000;
		String salt = "";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(numberOfEntries, salt);
		Assert.assertEquals(numberOfEntries, mpt.size());
		for(int i = 0; i < numberOfEntries; i++) {
			byte[] key = Utils.getKey(i);
			byte[] value = Utils.getValue(i, salt);
			Assert.assertTrue(Arrays.equals(mpt.get(key), value));
			if(i > 499) {
				mpt.delete(key);				
			}
		}
		Assert.assertEquals(500, mpt.size());
		for(int i = 0; i < numberOfEntries; i++) {
			byte[] key = Utils.getKey(i);
			byte[] value = Utils.getValue(i, salt);
			if(i > 499) {
				Assert.assertEquals(null, mpt.get(key));			
			}else {
				Assert.assertTrue(Arrays.equals(mpt.get(key), value));
			}
		}
		MPTDictionaryFull mpt2 = Utils.makeMPTDictionaryFull(500, salt);
		Assert.assertEquals(500, mpt2.size());
		Assert.assertTrue(Arrays.equals(mpt.commitment(), mpt2.commitment()));
	}
	
	@Test
	public void testTrieSerializationFullTrie() {
		List<Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(1000, "test");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		byte[] asbytes = mpt.serialize().toByteArray();
		try {
			MPTDictionaryFull mptFromBytes = MPTDictionaryFull.deserialize(asbytes);
			Assert.assertTrue(mptFromBytes.equals(mpt));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}	
	}
		
	@Test
	public void testEqualityBasic() {
		List<Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(1000, "test");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		// shuffle the kvpairs so that they are inserted in a different order
		Collections.shuffle(kvpairs);
		MPTDictionaryFull mpt2 = Utils.makeMPTDictionaryFull(kvpairs);
		MPTDictionaryFull mpt3 = Utils.makeMPTDictionaryFull(kvpairs.subList(0, 500));
		Assert.assertTrue(mpt.equals(mpt2));
		Assert.assertFalse(mpt.equals(mpt3));
	}
	
	@Test
	public void testEqualitySymmetric() {
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, "some salt changes the hash");
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
	public void testGetBitVariedManyBytes() {
		byte[] MANY_BYTES = new byte[] {9, 100, (byte) 200, 32};
		String binaryStr = "00001001" + "01100100" + "11001010" + "00100000";
		int[] expected = new int[32];
		for (int i = 0; i < 32; i++) {
			expected[i] = Character.getNumericValue(binaryStr.charAt(i));
		}
		for (int i = 0; i < 16; i++) {
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
	public void testMPTBasicUpdateSequenceChangeValues() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		byte[] value100 = CryptographicDigest.hash("100".getBytes());
		byte[] value101 = CryptographicDigest.hash("101".getBytes());
		byte[] value102 = CryptographicDigest.hash("102".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// mark everything as changed
		mpt.reset();

		// copy a path to a key
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyF);
		
		
		// change value
		mpt.insert(keyC, value100);
		mpt.insert(keyD, value101);
		mpt.insert(keyE, value102);		
		
		// calculate a new path to a key
		MPTDictionaryPartial newPath = new MPTDictionaryPartial(mpt, keyF);		
		
		// save the changes 
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);

		// use the changes to calculate an update for the original path
		MerklePrefixTrie updates = changes.getUpdates(keyF);
			
		try {
			// update the original path
			path.processUpdates(updates);			
			// should produce the new path
			Assert.assertTrue(newPath.equals(path));
		} catch (InvalidSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTBasicUpdateSequenceInsertValues() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		byte[] keyG = CryptographicDigest.hash("G".getBytes());
		byte[] keyH = CryptographicDigest.hash("H".getBytes());
		byte[] keyI = CryptographicDigest.hash("I".getBytes());
		byte[] keyJ = CryptographicDigest.hash("J".getBytes());

		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		byte[] value100 = CryptographicDigest.hash("100".getBytes());
		byte[] value101 = CryptographicDigest.hash("101".getBytes());
		byte[] value102 = CryptographicDigest.hash("102".getBytes());
		byte[] value103 = CryptographicDigest.hash("103".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// mark everything as changed
		mpt.reset();

		// copy a path to a key
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyF);		
		
		// change values
		mpt.insert(keyG, value100);
		mpt.insert(keyH, value101);
		mpt.insert(keyI, value102);
		mpt.insert(keyJ, value103);
		
		// calculate a new path to a key
		MPTDictionaryPartial newPath = new MPTDictionaryPartial(mpt, keyF);
		
		// save the changes 
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);

		try {
			// use the changes to calculate an update for the original path
			MerklePrefixTrie updates = changes.getUpdates(keyF);
			// update the original path
			path.processUpdates(updates);
			
			// should produce the new path
			Assert.assertTrue(newPath.equals(path));
		} catch (InvalidSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTBasicUpdateSequenceDeleteValues() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// mark everything as changed
		mpt.reset();

		// copy a path to a key
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyF);		

		// delete values
		mpt.delete(keyA);
		mpt.delete(keyB);
		mpt.delete(keyC);
		mpt.delete(keyD);

		// calculate a new path to a key
		MPTDictionaryPartial newPath = new MPTDictionaryPartial(mpt, keyF);
		
		// save the changes 
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);

	try {
			// use the changes to calculate an update for the original path
			MerklePrefixTrie updates = changes.getUpdates(keyF);
			// update the original path
			path.processUpdates(updates);

			// should produce the new path
			Assert.assertTrue(newPath.equals(path));
		} catch (InvalidSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testMPTBasicUpdateSequenceDeleteValuesEntireSubtreeMovedUp() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		byte[] keyG = CryptographicDigest.hash("G".getBytes());
		byte[] keyH = CryptographicDigest.hash("H".getBytes());
		
		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// mark everything as changed
		mpt.reset();

		// copy a path to a key
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyB);		
		
		// delete values
		mpt.delete(keyA);
		mpt.delete(keyC);
		mpt.delete(keyD);
		mpt.delete(keyG);
		mpt.delete(keyH);
		
		// calculate a new path to a key
		MPTDictionaryPartial newPath = new MPTDictionaryPartial(mpt, keyB);
			
		// save the changes 
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);
		
		try {
			// use the changes to calculate an update for the original path
			MerklePrefixTrie updates = changes.getUpdates(keyB);
			// update the original path
			path.processUpdates(updates);
			
			// should produce the new path
			Assert.assertTrue(newPath.equals(path));
		} catch (InvalidSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testMPTBasicUpdateSequence() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		byte[] keyG = CryptographicDigest.hash("G".getBytes());

		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		byte[] value101 = CryptographicDigest.hash("101".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);
		
		// mark everything as changed
		mpt.reset();
		
		// copy a path to a key
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyF);
		
		// make some changes
		mpt.insert(keyC, value101);
		mpt.insert(keyG, value1);
		
		// calculate a new path to a key
		MPTDictionaryPartial newPath = new MPTDictionaryPartial(mpt, keyF);
		
		// save the changes 
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);
		try {
			// use the changes to calculate an update for the original path
			MerklePrefixTrie updates = changes.getUpdates(keyF);
			// update the original path
			path.processUpdates(updates);
			
			// should produce the new path
			Assert.assertTrue(newPath.equals(path));
		} catch (InvalidSerializationException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testDeltaGenerateUpdatesMultiplePaths() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();

		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		byte[] keyG = CryptographicDigest.hash("G".getBytes());

		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		byte[] value100 = CryptographicDigest.hash("100".getBytes());
		byte[] value101 = CryptographicDigest.hash("101".getBytes());
		
		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);

		// create a partial tree
		List<byte[]> keys = new ArrayList<>();
		keys.add(keyE);
		keys.add(keyF);
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt, keys);
		
		mpt.reset();
		mpt.insert(keyG, value100);
		mpt.insert(keyA, value101);

		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);

		MerklePrefixTrie updates = changes.getUpdates(keys);
		try {
			partialmpt.processUpdates(updates);
			Assert.assertTrue(Arrays.equals(mpt.get(keyE), partialmpt.get(keyE)));
			Assert.assertTrue(Arrays.equals(mpt.get(keyF), partialmpt.get(keyF)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));
			
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testDeltaGenerateInsertsDeletesAndChanges() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();

		byte[] keyA = CryptographicDigest.hash("A".getBytes());
		byte[] keyB = CryptographicDigest.hash("B".getBytes());
		byte[] keyC = CryptographicDigest.hash("C".getBytes());
		byte[] keyD = CryptographicDigest.hash("D".getBytes());
		byte[] keyE = CryptographicDigest.hash("E".getBytes());
		byte[] keyF = CryptographicDigest.hash("F".getBytes());
		byte[] keyG = CryptographicDigest.hash("G".getBytes());

		byte[] value1 = CryptographicDigest.hash("1".getBytes());
		byte[] value2 = CryptographicDigest.hash("2".getBytes());
		byte[] value3 = CryptographicDigest.hash("3".getBytes());

		byte[] value100 = CryptographicDigest.hash("100".getBytes());
		byte[] value101 = CryptographicDigest.hash("101".getBytes());

		// insert the entries
		mpt.insert(keyA, value1);
		mpt.insert(keyB, value2);
		mpt.insert(keyC, value3);
		mpt.insert(keyD, value3);
		mpt.insert(keyE, value2);
		mpt.insert(keyF, value1);

		// create a partial tree
		List<byte[]> keys = new ArrayList<>();
		keys.add(keyE);
		keys.add(keyF);
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt, keys);
		
		mpt.reset();
		mpt.insert(keyG, value100);
		mpt.insert(keyA, value101);
		mpt.delete(keyB);
		
		MPTDictionaryPartial partialmptNew = new  MPTDictionaryPartial(mpt, keys);

		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);

		MerklePrefixTrie updates = changes.getUpdates(keys);
		try {
			partialmpt.processUpdates(updates);
			Assert.assertEquals(partialmptNew, partialmpt);
			
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testDeltaGenerateInsertsDeletesAndChangesLargeMpt() {
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, "");
		byte[] key1 = Utils.getKey(112);
		byte[] key2 = Utils.getKey(204);
		byte[] key3 = Utils.getKey(681);
		byte[] key4 = Utils.getKey(939);
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		keys.add(key2);
		keys.add(key3);
		keys.add(key4);
		MPTDictionaryPartial paths = new MPTDictionaryPartial(mpt, keys);
		
		mpt.reset();
		// now delete some keys 
		for(int key = 300; key < 400; key++) {
			final byte[] keyByte = Utils.getKey(key);
			mpt.delete(keyByte);
		}
		// insert some new keys
		for(int key = 1000; key < 2000; key++) {
			final byte[] keyByte = Utils.getKey(key);
			final byte[] valueByte = Utils.getValue(key, "");
			mpt.insert(keyByte, valueByte);
		}
		// modify some key-value mappings
		for(int key = 0; key < 200; key++) {
			final byte[] keyByte = Utils.getKey(key);
			final byte[] valueByte = Utils.getValue(key, "NEW");
			mpt.insert(keyByte, valueByte);
		}
		
		// update paths
		MPTDictionaryPartial newPaths = new MPTDictionaryPartial(mpt, keys);
		
		MPTDictionaryDelta changes = new MPTDictionaryDelta(mpt);
		MerklePrefixTrie updates = changes.getUpdates(keys);
		try {
			paths.processUpdates(updates);
			Assert.assertEquals(newPaths, paths);
		}catch(Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
	}
	
}
