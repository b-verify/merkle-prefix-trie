package mpt.dictionary.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mpt.core.InvalidSerializationException;
import mpt.core.Node;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;
import serialization.MptSerialization;

public class MPTPartialExposedTest {
	
	private class MPTPartialExposed extends MPTDictionaryPartial {

		private MPTPartialExposed(MPTDictionaryFull fullMPT, List<byte[]> keys) {
			super(fullMPT, keys);
		}
		
		private Node getRoot() {
			return this.root;
		}
		
	}
	
	/**
	 * Testing generating partial tries
	 */
	
	@Test
	public void testPartialInvalidKeys() {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		
		
		Node root = partial.getRoot();
		
	}
	
	@Test
	public void testPartialEmptyKeys() {
		
	}
	
	@Test
	public void testPartialOneKey() {
		
	}
	
	@Test
	public void testPartialManyKeysFullTrie() {
		
		//get full tree
		
		//check paths for 3 keys
		
	}
	
	@Test
	public void testPartialManyKeysUnbalancedTrie() {
		
		//10
		//1101
		//1100
		//0001
		//0000
		//001
	}
	
	/**
	 * Testing processUpdates for single key
	 */
	
	@Test
	public void testProcessUpdatesNoChange() {
		
	}

	@Test
	public void testProcessUpdatesInsert() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
	}
	
	@Test
	public void testProcessUpdatesInsertIntoEmptyLeaf() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
	}
	
	@Test
	public void testProcessUpdatesInsertLongSplit() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
	}
	
	@Test
	public void testProcessUpdatesInsertShortSplit() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
	}
	
	@Test
	public void testProcessUpdatesInsertUpdateValue() throws InvalidSerializationException {
		
		MPTDictionaryFull trie = new MPTDictionaryFull();
		
		List<byte[]> keys = new ArrayList<>();
		
		MPTPartialExposed partial = new MPTPartialExposed(trie, keys);
		
		MPTDictionaryDelta delta = new MPTDictionaryDelta(trie);
		
		MptSerialization.MerklePrefixTrie deltaSerialized = delta.getUpdates(keys);
		
		partial.processUpdates(deltaSerialized);
		
		Node root = partial.getRoot();
		
		//inspect structure of partial
		
	}
	
	
	
	@Test
	public void testProcessUpdatesDeleteRemoveLeafNode() {
		
		//
		
	}
	
	@Test
	public void testProcessUpdatesDeleteSmallShrink() {
		
		//
		
	}
	
	@Test
	public void testProcessUpdatesDeleteBigShrink() {
		
		//
		
	}
	
	@Test
	public void testProcessUpdatesChangeInIrrelevantKeys() {
		
	}
	
	/**
	 * Testing processUpdate for multiple keys
	 */
	
	@Test
	public void testProcessUpdatesChangeInOneRelevantKey() {
		
	}
	
	//TODO how often does client ask for changes? can it do batch-updates, given a list of
	//all the deltas since the last processUpdates?
	//or are we guaranteed that server gives client the correct order of updates?
	//if so, is server entirely responsible for giving the same 
	
	
}
