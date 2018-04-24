package mpt.dictionary.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import mpt.core.InsufficientAuthenticationDataException;
import mpt.core.InvalidSerializationException;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;

public class MPTDictionaryPartialTest {
	
	@Test
	public void testCopySinglePathDepth1() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		byte[] bytes = new byte[] {0};
		try {
			mpt.insert(bytes, "1".getBytes());
			MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, bytes);
			Assert.assertTrue("expect path contains correct entry", Arrays.equals("1".getBytes(), path.get(bytes)));
			Assert.assertTrue("expect same commitment of trie and path", Arrays.equals(mpt.commitment(), path.commitment()));	
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreatePartialTrieBasic() {
			MPTDictionaryFull mpt = new MPTDictionaryFull();

		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		

		System.out.println("\noriginal:\n"+mpt);

		// create a partial tree
		byte[] key = "F".getBytes();
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt,  key);
		System.out.println("\npartial:\n"+partialmpt);
		
		try {
			Assert.assertTrue(Arrays.equals("1".getBytes(), partialmpt.get(key)));
			Assert.assertTrue(Arrays.equals(mpt.commitment(), partialmpt.commitment()));	
		} catch (InsufficientAuthenticationDataException e) {
			Assert.fail(e.getMessage());
		}

	}
	
	@Test
	public void testCopyTwoPathsAdjacentKeysInMPT() {
		try {
			MPTDictionaryFull mpt = new MPTDictionaryFull();
			byte[] first = new byte[] {0};
			byte[] second = new byte[] {1};
			List<byte[]> keys = new ArrayList<>();
			keys.add(first);
			keys.add(second);
			mpt.insert(first, "1".getBytes());
			mpt.insert(second, "2".getBytes());	
			MPTDictionaryPartial path0 = new MPTDictionaryPartial(mpt, keys);
			Assert.assertArrayEquals(path0.commitment(), mpt.commitment());
			Assert.assertArrayEquals("1".getBytes(), path0.get(first));
			Assert.assertArrayEquals("2".getBytes(), path0.get(second));
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreatePartialTrieMultiplePaths() {
		MPTDictionaryFull mpt = new MPTDictionaryFull();

		// insert the entries
		mpt.insert("A".getBytes(), "1".getBytes());
		mpt.insert("B".getBytes(), "2".getBytes());
		mpt.insert("C".getBytes(), "3".getBytes());
		mpt.insert("D".getBytes(), "3".getBytes());		
		mpt.insert("E".getBytes(), "2".getBytes());		
		mpt.insert("F".getBytes(), "1".getBytes());		

		System.out.println("\noriginal:\n"+mpt);

		// create a partial tree
		byte[] key1 = "E".getBytes();
		byte[] key2 = "F".getBytes();
		List<byte[]> keys = new ArrayList<>();
		keys.add(key1);
		keys.add(key2);
		MPTDictionaryPartial partialmpt = new  MPTDictionaryPartial(mpt, keys);
		System.out.println("\npartial:\n"+partialmpt);
		try {
			Assert.assertTrue(Arrays.equals("2".getBytes(), partialmpt.get(key1)));
			Assert.assertTrue(Arrays.equals("1".getBytes(), partialmpt.get(key2)));
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
			for(int key = 0; key < n; key++) {
				String keyString = "key"+Integer.toString(key);
				String valueString = "value"+Integer.toString(key)+salt;
				MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyString.getBytes());
				byte[] value = path.get(keyString.getBytes());
				Assert.assertTrue("path should contain correct (key,value)", Arrays.equals(valueString.getBytes(), value));
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
				int key = n+offset;
				String keyString = "key"+Integer.toString(key);
				// copy path here should map a path to an empty leaf or a leaf
				// with a different key - so when we call get on the path it 
				// it returns nulls
				MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyString.getBytes());
				byte[] value = path.get(keyString.getBytes());
				Assert.assertTrue("not in tree - path should map to empty leaf", value == null);	
			}
		}catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testPathSerialization() {
		int key = 100;
		String salt = "serialization";
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(1000, salt);
		String keyString = "key"+Integer.toString(key);
		String valueString = "value"+Integer.toString(key)+salt;
		MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, keyString.getBytes());
		byte[] serialization = path.serialize().toByteArray();
		try {
			MPTDictionaryPartial fromBytes = MPTDictionaryPartial.deserialize(serialization);
			Assert.assertTrue("deserialized path contains the specific entry", 
					Arrays.equals(fromBytes.get(keyString.getBytes()), valueString.getBytes()));
			Assert.assertTrue("deserialized path commitment matches" ,
					Arrays.equals(fromBytes.commitment(), mpt.commitment()));
		} catch (InvalidSerializationException | InsufficientAuthenticationDataException e) {
			Assert.fail(e.getMessage());
		}
	}
}
