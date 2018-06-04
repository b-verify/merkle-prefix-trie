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

	public static void runExperiment(int testSize) {
		logger.log(Level.INFO, "running a throughput benchmark for "+testSize+" updates");
		List<Map.Entry<byte[], byte[]>> kvpairs = Utils.getKeyValuePairs(testSize, "throughput");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairs);
		Scanner sc = new Scanner(System.in);
		/*
		 * Request proofs in parallel for EVERY ADS
		 */
		logger.log(Level.INFO, "[Press enter to start updates (single threaded)]");
		sc.nextLine();
		logger.log(Level.INFO, "...starting benchmark");
		byte[] updatedValue = CryptographicDigest.hash("some stuff".getBytes());
		long startTime1 = System.currentTimeMillis();
		for(Map.Entry<byte[], byte[]> kvpair : kvpairs) {
			mpt.insert(kvpair.getKey(), updatedValue);
		}
		long endTime1 = System.currentTimeMillis();
		logger.log(Level.INFO, "...updates done");
		int nNodes = mpt.countNodes();
		int nHashes = mpt.countHashesRequiredToCommit();
		logger.log(Level.INFO, "[Press enter to commit updates (multi threaded)]");
		ExecutorService executorService = Executors.newCachedThreadPool();
		long startTime2 = System.currentTimeMillis();
		mpt.commitmentParallelized(executorService);
		long endTime2 = System.currentTimeMillis();		

		long duration1 = endTime1 - startTime1;
		long duration2 = endTime2 - startTime2;
		String timeTaken1 = formatter.format(duration1 / 1000d)+ " seconds";
		String timeTaken2 = formatter.format(duration2 / 1000d)+ " seconds";
		logger.log(Level.INFO, "Time taken to PERFORM "+testSize+" updates "+timeTaken1);
		logger.log(Level.INFO, "Time taken to COMMIT "+testSize+" (nodes: "+nNodes+"| hashes required: "+nHashes+
				") "+timeTaken2);
		logger.log(Level.INFO, "...done!");
		sc.close();	
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
		runExperiment(1000000);
	}
}
