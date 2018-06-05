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
	}
	
	public MPTDictionaryFull performSingleUpdate() {
		// update value of an existing key
		this.mptToCommit.insert(this.kvpairs.get(0).getKey(), NEW_VALUE);
		return this.mptToCommit;
	}
	
	public MPTDictionaryFull performSingleInsert() {
		// put in a new key-value
		this.mptToCommit.insert(NEW_KEY, NEW_VALUE);
		return this.mptToCommit;
	}
	
	public MPTDictionaryFull performSingleDelete() {
		// delete a key-value mapping
		this.mptToCommit.delete(this.kvpairs.get(0).getKey());
		return this.mptToCommit;
	}
	
	public byte[] commitSingleThreaded() {
		return this.mptToUpdate.commitment();
	}
	
	public byte[] commitParallelized(ExecutorService workers) {
		return this.mptToCommit.commitmentParallelized(workers);
	}
	
}
