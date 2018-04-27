package bench;

import java.math.BigInteger;

import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;

public class MerklePathSizeBenchmark {
	
	public static void main(String[] args) {
		int n = 10000000;
		String salt = "size benchmark";
		// make a MPT with 10^7 entries
		System.out.println("adding kv pairs");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(n, salt);
		System.out.println("done- calculating commitment");
		byte[] commitment = mpt.commitment();
		System.out.println("commitment: "+Utils.byteArrayAsHexString(commitment));
		byte[] serialization = mpt.serialize().toByteArray();
		int sizeEntireTrieSerialization = serialization.length;
		System.out.println("size of entire mpt	(bytes): "+sizeEntireTrieSerialization);
		
		// check size
		BigInteger total_size = BigInteger.ZERO;
		int max_size = 0;
		for(int i = 0; i < n; i++) {
			byte[] key = Utils.getKey(i);
			MPTDictionaryPartial path = new MPTDictionaryPartial(mpt, key);
			byte[] serialized = path.serialize().toByteArray();
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
