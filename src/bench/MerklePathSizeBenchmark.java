package bench;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpt.MerklePrefixTrie;

public class MerklePathSizeBenchmark {
	
	public static MerklePrefixTrie makeMerklePrefixTrie(int numberOfEntries, String salt) {
		return MerklePathSizeBenchmark.makeMerklePrefixTrie(
				MerklePathSizeBenchmark.getKeyValuePairs(numberOfEntries, salt));
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
	
	public static void main(String[] args) {
		int n = 10000;
		String salt = "size benchmark";
		List<Map.Entry<String, String>> kvpairs = MerklePathSizeBenchmark.getKeyValuePairs(n, salt);
		// make a MPT with 10k entries
		MerklePrefixTrie mpt = MerklePathSizeBenchmark.makeMerklePrefixTrie(kvpairs);
		BigInteger total_size = BigInteger.ZERO;
		int max_size = 0;
		for(Map.Entry<String, String> kvpair : kvpairs) {
			MerklePrefixTrie path = mpt.copyPath(kvpair.getKey().getBytes());
			byte[] serialized = path.serialize();
			int size = serialized.length;
			total_size = total_size.add(BigInteger.valueOf(size));
			if (size > max_size) {
				max_size = size;
			}
		}
		BigInteger average_size = total_size.divide(BigInteger.valueOf(n));
		System.out.println("Average proof size (bytes): "+average_size);
		System.out.println("Max proof size     (bytes): "+max_size);
		
		
	}
}
