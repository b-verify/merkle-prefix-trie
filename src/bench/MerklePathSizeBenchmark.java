package bench;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;

public class MerklePathSizeBenchmark {
	
	
	public static void benchmarkProofSizes(int nEntries, int nUpdates) {
		String startingSalt = "starting-entry";
		List<Map.Entry<byte[], byte[]>> kvpairsStart = Utils.getKeyValuePairs(nEntries, startingSalt);
		System.out.println("--------- building the MPT ---------");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairsStart);
		byte[] commitment = mpt.commitment();
		System.out.println("--------- calculating commitment ---------");
		System.out.println(Utils.byteArrayAsHexString(commitment));
		mpt.reset();
		System.out.println("--------- making updates ---------");
		List<byte[]> keys = kvpairsStart.stream().map(x -> x.getKey()).collect(Collectors.toList());
		Collections.shuffle(keys);
		// select some random keys to update
		List<byte[]> keysToUpdate = keys.subList(0, nUpdates);
		int i = 0;
		String newSalt = "new salt";
		for(byte[] key : keysToUpdate) {
			byte[] newValue = Utils.getValue(i, newSalt);
			mpt.insert(key, newValue);
		}
		System.out.println("--------- updates done, making delta and calculating new commitment ---------");
		commitment = mpt.commitment();
		MPTDictionaryDelta delta = new MPTDictionaryDelta(mpt);
		System.out.println(Utils.byteArrayAsHexString(commitment));
		System.out.println("--------- calculating proof sizes ---------");
		System.out.println("checking "+keys.size()+" keys");
		BigInteger totalFull = BigInteger.ZERO;
		BigInteger totalUpdate = BigInteger.ZERO;
		for(byte[] key : keys) {
			MPTDictionaryPartial fullPath = new MPTDictionaryPartial(mpt, key);
			int fullPathSerialziationSize = fullPath.serialize().toByteArray().length;
			int onlyUpdateSerializationSize = delta.getUpdates(key).toByteArray().length;
			totalFull = totalFull.add(BigInteger.valueOf(fullPathSerialziationSize));
			totalUpdate = totalUpdate.add(BigInteger.valueOf(onlyUpdateSerializationSize));
		}
		BigInteger avgUpdate = totalUpdate.divide(BigInteger.valueOf(keys.size()));
		BigInteger avgFull = totalFull.divide(BigInteger.valueOf(keys.size()));
		System.out.println("average full : "+avgFull+" | average update : "+avgUpdate);
	}
	
	
	public static void main(String[] args) {
		
		benchmarkProofSizes(1000000, 100);
	}		
		
}
