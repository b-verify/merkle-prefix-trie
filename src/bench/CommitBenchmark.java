package bench;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import crypto.CryptographicDigest;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;

/**
 * This is a dedicated benchmarking program for measuring the 
 * speed of the commitment calculation that should be exposed to 
 * benchmarking harness such as JMH. 
 * 
 * It is good practice to separate the benchmarking harness from the 
 * rest of the library so I have put the harness which runs this 
 * test in a separate repo.
 * 
 * @author henryaspegren
 *
 */
public class CommitBenchmark {
	private static final Logger logger = Logger.getLogger(CommitBenchmark.class.getName());	
	private MPTDictionaryFull mpt;
	
	public CommitBenchmark(int n, int nBatchSize) {
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(n, "benchmark");
		this.mpt = Utils.makeMPTDictionaryFull(kvpairs);
		
		byte[] initialCommit = this.mpt.commitment();
		logger.log(Level.INFO, "...initial commitment: "+
					Utils.byteArrayAsHexString(initialCommit));
		
		// now do a bunch of updates, but DO NOT commit
		int i = 0;
		List<Map.Entry<byte[], byte[]>> toUpdate = kvpairs.subList(0, nBatchSize);
		for(Map.Entry<byte[], byte[]> kvpair : toUpdate) {
			byte[] newVal = CryptographicDigest.hash(
					(Integer.toString(i)+"sdsfs").getBytes());
			this.mpt.insert(kvpair.getKey(), newVal);
			i+= 1;
		}
		
		logger.log(Level.INFO, "..."+nBatchSize+" updates applied but not committed");
		logger.log(Level.INFO, "...# of nonempty leafs: "+this.mpt.countNonEmptyLeafNodes());
		logger.log(Level.INFO, "...# of empty leafs: "+this.mpt.countEmptyLeafNodes());
		logger.log(Level.INFO, "...# of interior nodes: "+this.mpt.countInteriorNodes());
		logger.log(Level.INFO, "...# of hashes required to commit: "+this.mpt.countHashesRequiredToCommit());
	}
	
	public byte[] performCommit() {
		return this.mpt.commitment();
	}
	
}
