package mpt.set.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import mpt.core.Utils;
import mpt.set.MPTSetFull;

public class MPTSetFullTest {

	@Test
	public void testInsert() {
		List<byte[]> values = Utils.getValues(1000, "set salt");
		MPTSetFull mpt = Utils.makeMPTSetFull(values);
		for(byte[] value : values) {
			Assert.assertTrue(mpt.inSet(value));
		}
		for(int i = 0; i < values.size() ; i++) {
			byte[] notInSet = Utils.getValue(i, "other salt");
			Assert.assertFalse(mpt.inSet(notInSet));
		}
	}
	
	@Test 
	public void testDelete() {
		List<byte[]> values = Utils.getValues(1000, "set salt");
		MPTSetFull mpt = Utils.makeMPTSetFull(values);
		// delete the first 250 values
		int i = 0;
		for(byte[] value : values) {
			if(i > 250) {
				break;
			}
			mpt.delete(value);
		}
		// check that the set is correct
		for(byte[] value : values) {
			if(i > 250) {
				Assert.assertTrue(mpt.inSet(value));
			}else {
				Assert.assertFalse(mpt.inSet(value));
			}
		}
	}
	
	@Test
	public void testCommitmentAndEquality() {
		List<byte[]> values = Utils.getValues(1000, "set salt");
		MPTSetFull mpt1 = Utils.makeMPTSetFull(values);
		// permute the values 
		Collections.shuffle(values);
		MPTSetFull mpt2 = Utils.makeMPTSetFull(values);
		// we should get the same MPT
		// with the same commitment
		Assert.assertTrue(Arrays.equals(mpt1.commitment(), mpt2.commitment()));
		Assert.assertTrue(mpt1.equals(mpt2));
	}
	
	
	
	
}
