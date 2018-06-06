package bench;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import crypto.CryptographicDigest;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;

/**
 * This is a dedicated benchmarking program for measuring the 
 * speed of updates in the MPT that should be exposed to 
 * benchmarking harness such as JMH. 
 * 
 * It is good practice to separate the benchmarking harness from the 
 * rest of the library so I have put the harness which runs this 
 * test in a separate repo.
 * 
 * @author henryaspegren
 *
 */
public class UpdateBenchmark {
	private static final Logger logger = Logger.getLogger(UpdateBenchmark.class.getName());	
	private static final byte[] NEW_VALUE = CryptographicDigest.hash("some stuff".getBytes());

	private MPTDictionaryFull mpt;
	private List<byte[]> keysToUpdate;
	
	public UpdateBenchmark(int n, int nUpdates) {
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(n, "benchmark");
		this.mpt = Utils.makeMPTDictionaryFull(kvpairs);
		
		byte[] initialCommit = this.mpt.commitment();
		logger.log(Level.INFO, "...initial commitment: "+
					Utils.byteArrayAsHexString(initialCommit));
		
		this.keysToUpdate = kvpairs.subList(0, nUpdates)
				.stream()
				.map(x -> x.getKey())
				.collect(Collectors.toList());
		
	}
	
	public MPTDictionaryFull performUpdates() {
		for(byte[] adsId : this.keysToUpdate) {
			this.mpt.insert(adsId, NEW_VALUE);
		}
		return this.mpt;
	}

}
