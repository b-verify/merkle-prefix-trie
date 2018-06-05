package bench;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import crypto.CryptographicDigest;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;

/**
 * This class exposes a benchmark interface that should be used
 * by a Java microbenchmarking framework (e.g. JMH). 
 * 
 * It is good style to separate the benchmarking framework from 
 * the library code that is being tested. To conform to this style
 * the actual code required to run the benchmark is stored in a
 * separate repo.
 * 
 * @author henryaspegren
 *
 */
public class ThroughputBenchmark {
	private static final Logger logger = Logger.getLogger(ThroughputBenchmark.class.getName());	
	private static final byte[] NEW_VALUE = CryptographicDigest.hash("some stuff".getBytes());
	private static final byte[] NEW_KEY = CryptographicDigest.hash("-1".getBytes());

	private MPTDictionaryFull mptToUpdate;
	private MPTDictionaryFull mptToCommit;
	private List<Map.Entry<byte[], byte[]>> kvpairs;
	
	public ThroughputBenchmark(int n, int nUpdatesToCommit) {
		this.kvpairs = Utils.getKeyValuePairs(n, "throughput");
		
		// mpt to update
		this.mptToUpdate = Utils.makeMPTDictionaryFull(this.kvpairs);
		// mpt to commit
		this.mptToCommit = Utils.makeMPTDictionaryFull(this.kvpairs);
		
		byte[] initialCommit = this.mptToUpdate.commitment();
		byte[] initialCommitment2 = this.mptToCommit.commitment();
		
		// should always be the same for deterministic benchmark
		logger.log(Level.INFO, "...initial commitments: "+Utils.byteArrayAsHexString(initialCommit)+
				"|"+Utils.byteArrayAsHexString(initialCommitment2));	
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdatesToCommit)) {
			this.mptToCommit.insert(kvpair.getKey(), NEW_VALUE);
		}
		logger.log(Level.INFO, "...# of nonempty leafs: "+this.mptToCommit.countNonEmptyLeafNodes());
		logger.log(Level.INFO, "...# of empty leafs: "+this.mptToCommit.countEmptyLeafNodes());
		logger.log(Level.INFO, "...# of interior nodes: "+this.mptToCommit.countInteriorNodes());
		logger.log(Level.INFO, "...# of hashes required to commit: "+this.mptToCommit.countHashesRequiredToCommit());
	}
	
	public MPTDictionaryFull performSingleUpdate() {
		// update value of an existing key
		this.mptToUpdate.insert(this.kvpairs.get(0).getKey(), NEW_VALUE);
		return this.mptToUpdate;
	}
	
	public MPTDictionaryFull performSingleInsert() {
		// put in a new key-value
		this.mptToUpdate.insert(NEW_KEY, NEW_VALUE);
		return this.mptToUpdate;
	}
	
	public MPTDictionaryFull performSingleDelete() {
		// delete a key-value mapping
		this.mptToUpdate.delete(this.kvpairs.get(0).getKey());
		return this.mptToUpdate;
	}
	
	public byte[] commitSingleThreaded() {
		return this.mptToCommit.commitment();
	}
	
	public byte[] commitParallelized(ExecutorService workers) {
		return this.mptToCommit.commitmentParallelized(workers);
	}
	
}
