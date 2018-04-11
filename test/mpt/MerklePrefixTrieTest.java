package mpt;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class MerklePrefixTrieTest {
	
	public static MerklePrefixTrie makeMerklePrefixTrie(int numberOfEntries, String salt) {
		MerklePrefixTrie mpt = new MerklePrefixTrie();
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			mpt.set(keyString.getBytes(), valueString.getBytes());
		}
		return mpt;
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
		
		// insert them again - should not change tree since already inserted
		Assert.assertFalse(mpt.set("A".getBytes(), "1".getBytes()));
		Assert.assertFalse(mpt.set("B".getBytes(), "2".getBytes()));
		Assert.assertFalse(mpt.set("C".getBytes(), "3".getBytes()));
		Assert.assertFalse(mpt.set("D".getBytes(), "3".getBytes()));		
		Assert.assertFalse(mpt.set("E".getBytes(), "2".getBytes()));		
		Assert.assertFalse(mpt.set("F".getBytes(), "1".getBytes()));
		
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
	
		// remove them 
		Assert.assertTrue(mpt.deleteKey("B".getBytes()));
		Assert.assertEquals(null, mpt.get("B".getBytes()));
		Assert.assertTrue(mpt.deleteKey("D".getBytes()));
		Assert.assertEquals(null, mpt.get("D".getBytes()));
		Assert.assertTrue(mpt.deleteKey("F".getBytes()));
		Assert.assertEquals(null, mpt.get("F".getBytes()));
		
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
		int numberOfEntries = 10000;
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
		int numberOfEntries = 10000;
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
		int numberOfEntries = 10000;
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
		int numberOfEntries = 10000;
		String salt = "";
		MerklePrefixTrie mpt = MerklePrefixTrieTest.makeMerklePrefixTrie(numberOfEntries, salt);
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			Assert.assertTrue(Arrays.equals(mpt.get(keyString.getBytes()), valueString.getBytes()));
			if(key > 4999) {
				Assert.assertTrue(mpt.deleteKey(keyString.getBytes()));				
			}
		}
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			if (key > 4999) {
				Assert.assertEquals(null, mpt.get(keyString.getBytes()));			
			}else {
				Assert.assertTrue(Arrays.equals(mpt.get(keyString.getBytes()), valueString.getBytes()));
			}
		}
		MerklePrefixTrie mpt2 = MerklePrefixTrieTest.makeMerklePrefixTrie(5000, salt);
		Assert.assertTrue(Arrays.equals(mpt.getCommitment(), mpt2.getCommitment()));
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
	
}
