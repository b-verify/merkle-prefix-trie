package mpt;

import org.junit.Assert;
import org.junit.Test;

public class MerklePrefixTrieTest {
	
	@Test
	public void testGetBit() {
		// for all zeros all bits should be zero
		for(int i = 0; i < 32; i++) {
			Assert.assertFalse(MerklePrefixTrie.getBit(new byte[]{0, 0, 0, 0}, i));
		}
		// for all ones all bits should be one
		for(int i = 0; i < 32; i++) {
			Assert.assertTrue(MerklePrefixTrie.getBit(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}, i));
		}

	}
	
}
