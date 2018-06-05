package bench;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import crypto.CryptographicDigest;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;

public class ThroughputBenchmark {
	private static final Logger logger = Logger.getLogger(ThroughputBenchmark.class.getName());	
	private static final byte[] NEW_VALUE = CryptographicDigest.hash("some stuff".getBytes());
	
	private MPTDictionaryFull mpt;
	private List<Map.Entry<byte[], byte[]>> kvpairs;
	private int nUpdates;
	private int n;
	
	public ThroughputBenchmark(int n, int nUpdates) {
		this.nUpdates = nUpdates;
		this.n = n;
		
		this.kvpairs = Utils.getKeyValuePairs(this.n, "throughput");
		
		// mpt to update
		this.mpt = Utils.makeMPTDictionaryFull(this.kvpairs);
		byte[] initialCommit = this.mpt.commitment();
		
		// should always be the same for deterministic benchmark
		logger.log(Level.INFO, "...initial commitment: "+Utils.byteArrayAsHexString(initialCommit));		
	}
	
	public void printPerformUpdateDetails() {
		MPTDictionaryFull mptForDetails = Utils.makeMPTDictionaryFull(this.kvpairs);
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdates)) {
			mptForDetails.insert(kvpair.getKey(), NEW_VALUE);
		}
		int nEmptyLeafs = mptForDetails.countEmptyLeafNodes();
		int nNonEmptyLeafs = mptForDetails.countNonEmptyLeafNodes();
		int nInterior = mptForDetails.countInteriorNodes();	
		int nHashes = mptForDetails.countHashesRequiredToCommit();
		logger.log(Level.INFO, "empty leafs: "+nEmptyLeafs
				+"| nonempty leafs: "+nNonEmptyLeafs+"| interior: "+nInterior);
		logger.log(Level.INFO, "hashes required to commit: "+nHashes);
	}
	
	
	public byte[] performUpdatesSingleThreadedCommits() {
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdates)) {
			this.mpt.insert(kvpair.getKey(), NEW_VALUE);
		}
		byte[] result = this.mpt.commitment();
		return result;

	}
	
	
	public byte[] performUpdatesParallelizedCommits(ExecutorService workers) {
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdates)) {
			this.mpt.insert(kvpair.getKey(), NEW_VALUE);
		}
		byte[] result = this.mpt.commitmentParallelized(workers);
		return result;	
	}
	
}
