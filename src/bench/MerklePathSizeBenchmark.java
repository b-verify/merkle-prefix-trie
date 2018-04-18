package bench;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import mpt.MerklePrefixTrie;
import mpt.Utils;


public class MerklePathSizeBenchmark {
	
	public static void main(String[] args) {
		int n = 10000;
		String salt = "size benchmark";
		List<Map.Entry<String, String>> kvpairs = Utils.getKeyValuePairs(n, salt);
		// make a MPT with 10k entries
		MerklePrefixTrie mpt = Utils.makeMerklePrefixTrie(kvpairs);
		byte[] serialization = mpt.serialize();
		int sizeEntireTrieSerialization = serialization.length;
		// check size
		System.out.println("size of entire mpt	(bytes): "+sizeEntireTrieSerialization);
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
		System.out.println("Average proof size 	(bytes): "+average_size);
		System.out.println("Max proof size     	(bytes): "+max_size);
	}
}
