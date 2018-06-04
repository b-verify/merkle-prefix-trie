package bench;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import crypto.CryptographicDigest;
import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryFull;

public class ThroughputBenchmark {
	private static final NumberFormat formatter = new DecimalFormat("#0.000");
	private static final Logger logger = Logger.getLogger(ThroughputBenchmark.class.getName());

	public static void runExperimentSingleThreaded(int testSize, int nUpdates, Scanner sc) {
		logger.log(Level.INFO, "...single threaded benchmark");
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(testSize, "throughput");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		byte[] initialCommit = mpt.commitment();
		logger.log(Level.INFO, "...initial commitment: "+Utils.byteArrayAsHexString(initialCommit));
		int nEmptyLeafs = mpt.countEmptyLeafNodes();
		int nNonEmptyLeafs = mpt.countNonEmptyLeafNodes();
		int nInterior = mpt.countInteriorNodes();
		logger.log(Level.INFO, "empty leafs: "+nEmptyLeafs
				+"| nonempty leafs: "+nNonEmptyLeafs+"| interior: "+nInterior);
		logger.log(Level.INFO, "[Press enter to start updates (single threaded)]");
		sc.nextLine();
		logger.log(Level.INFO, "...starting benchmark");
		byte[] updatedValue = CryptographicDigest.hash("some stuff".getBytes());
		long startTime1 = System.currentTimeMillis();
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdates)) {
			mpt.insert(kvpair.getKey(), updatedValue);
		}
		long endTime1 = System.currentTimeMillis();
		logger.log(Level.INFO, "...updates done");
		int nHashes = mpt.countHashesRequiredToCommit();
		logger.log(Level.INFO, "hashes required to commit: "+nHashes);
		logger.log(Level.INFO, "[Press enter to commit updates (single threaded)]");
		sc.nextLine();
		long startTime2 = System.currentTimeMillis();
		byte[] newCommitment  = mpt.commitment();
		long endTime2 = System.currentTimeMillis();		
		logger.log(Level.INFO, "...new commitment: "+Utils.byteArrayAsHexString(newCommitment));
		long duration1 = endTime1 - startTime1;
		long duration2 = endTime2 - startTime2;
		String timeTaken1 = formatter.format(duration1 / 1000d)+ " seconds";
		String timeTaken2 = formatter.format(duration2 / 1000d)+ " seconds";
		logger.log(Level.INFO, "Time taken to PERFORM "+nUpdates+" updates "+timeTaken1);
		logger.log(Level.INFO, "Time taken to COMMIT "+nUpdates+" updates "+timeTaken2);
		logger.log(Level.INFO, "...done!");
	}
	
	public static void runExperimentParallel(int testSize, int nUpdates, Scanner sc) {
		logger.log(Level.INFO, "...multi threaded benchmark");
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(testSize, "throughput");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		byte[] initialCommit = mpt.commitment();
		logger.log(Level.INFO, "...initial commitment: "+Utils.byteArrayAsHexString(initialCommit));
		int nEmptyLeafs = mpt.countEmptyLeafNodes();
		int nNonEmptyLeafs = mpt.countNonEmptyLeafNodes();
		int nInterior = mpt.countInteriorNodes();
		logger.log(Level.INFO, "empty leafs: "+nEmptyLeafs
				+"| nonempty leafs: "+nNonEmptyLeafs+"| interior: "+nInterior);
		
		logger.log(Level.INFO, "[Press enter to start updates (single threaded)]");
		sc.nextLine();
		logger.log(Level.INFO, "...starting benchmark");
		byte[] updatedValue = CryptographicDigest.hash("some stuff".getBytes());
		long startTime1 = System.currentTimeMillis();
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs.subList(0, nUpdates)) {
			mpt.insert(kvpair.getKey(), updatedValue);
		}
		long endTime1 = System.currentTimeMillis();
		logger.log(Level.INFO, "...updates done");
		int nHashes = mpt.countHashesRequiredToCommit();
		logger.log(Level.INFO, "hashes required to commit: "+nHashes);
		logger.log(Level.INFO, "[Press enter to commit updates (multi threaded)]");
		sc.nextLine();
		ExecutorService executorService = Executors.newCachedThreadPool();
		long startTime2 = System.currentTimeMillis();
		byte[] newCommitment = mpt.commitmentParallelized(executorService);
		long endTime2 = System.currentTimeMillis();		
		logger.log(Level.INFO, "...new commitment: "+Utils.byteArrayAsHexString(newCommitment));

		long duration1 = endTime1 - startTime1;
		long duration2 = endTime2 - startTime2;
		String timeTaken1 = formatter.format(duration1 / 1000d)+ " seconds";
		String timeTaken2 = formatter.format(duration2 / 1000d)+ " seconds";
		logger.log(Level.INFO, "Time taken to PERFORM "+nUpdates+" updates "+timeTaken1);
		logger.log(Level.INFO, "Time taken to COMMIT "+nUpdates+" updates "+timeTaken2);
		logger.log(Level.INFO, "...done!");
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		int n = 1000000;
		int nUpdates = 100000;
		logger.log(Level.INFO, "test size: "+n+" | number of updates: "+nUpdates);
		Scanner sc = new Scanner(System.in);
		runExperimentSingleThreaded(n, nUpdates, sc);
		runExperimentParallel(n, nUpdates, sc);
		sc.close();
	}
}
