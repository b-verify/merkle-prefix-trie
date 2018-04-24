package mpt.dictionary.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import crpyto.CryptographicDigest;
import mpt.core.InsufficientAuthenticationDataException;
import mpt.core.InvalidSerializationException;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;

public class MPTDictionaryPartialTest {
	
	@Test
	public void testCreatePartialTrieBasic() {
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

		// create a partial tree
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt,  keyF);
		
		try {
			Assert.assertTrue(Arrays.equals(value1, partialmpt.get(keyF)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));	
		} catch (InsufficientAuthenticationDataException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testCopyTwoPathsAdjacentKeysInMPT() {
		try {
			MPTDictionaryFull mpt = new MPTDictionaryFull();
			byte[] first = CryptographicDigest.hash(new byte[] {0});
			byte[] second = CryptographicDigest.hash(new byte[] {1});
			byte[] value1 = CryptographicDigest.hash("1".getBytes());
			byte[] value2 = CryptographicDigest.hash("2".getBytes());
			
			List<byte[]> keys = new ArrayList<>();
			keys.add(first);
			keys.add(second);
			mpt.insert(first, value1);
			mpt.insert(second, value2);	
			MPTDictionaryPartial path0 = new MPTDictionaryPartial(mpt, keys);
			Assert.assertArrayEquals(path0.commitment(), mpt.commitment());
			Assert.assertArrayEquals(value1, path0.get(first));
			Assert.assertArrayEquals(value2, path0.get(second));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreatePartialTrieMultiplePaths() {
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

		// create a partial tree
		List<byte[]> keys = new ArrayList<>();
		keys.add(keyE);
		keys.add(keyF);
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt, keys);
		try {
			Assert.assertTrue(Arrays.equals(value2, partialmpt.get(keyE)));
			Assert.assertTrue(Arrays.equals(value1, partialmpt.get(keyF)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));
		} catch (InsufficientAuthenticationDataException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCopyPathKeyPresent() {
		int n = 1000;
		String salt = "path test";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, salt);
		try {
			for(int i = 0; i < n; i++) {
				byte[] key = Utils.getKey(i);
				byte[] value = Utils.getValue(i, salt);
				MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, key);
				Assert.assertTrue(Arrays.equals(value, path.get(key)));
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCopyPathKeyNotPresent() {
		int n = 1000;
		String salt = "path test";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, salt);
		try {
			for(int offset = 1; offset < 1000; offset++) {
				// not in tree
				int keyIdx = n+offset;
				byte[] key = Utils.getKey(keyIdx);
				// copy path here should map a path to an empty leaf or a leaf
				// with a different key - so when we call get on the path it 
				// it returns nulls
				MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, key);
				byte[] value = path.get(key);
				Assert.assertTrue("not in tree - path should map to empty leaf", value == null);	
			}
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testPathSerialization() {
		int i = 100;
		String salt = "serialization";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, salt);
		byte[] key = Utils.getKey(i);
		byte[] value = Utils.getValue(i, salt);
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, key);
		byte[] serialization = path.serialize().toByteArray();
		try {
			MPTDictionaryPartial fromBytes = MPTDictionaryPartial.deserialize(serialization);
			Assert.assertTrue("deserialized path contains the specific entry", 
					Arrays.equals(fromBytes.get(key), value));
			Assert.assertTrue("deserialized path commitment matches" ,
					Arrays.equals(fromBytes.commitment(), mpt.commitment()));
		} catch (InvalidSerializationException | InsufficientAuthenticationDataException e) {
			Assert.fail(e.getMessage());
		}
	}
}
