package bench;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import mpt.core.Utils;
import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;

public class MerklePathSizeBenchmark {

	public static List<String> getCSVRow(int n, int nInterior, int nNonEmptyLeaf, 
			int nEmptyLeaf, int updates, int nHashesRequiredToUpdate,
			BigInteger totalFull, BigInteger totalUpdate,
			BigInteger avgFull, BigInteger avgUpdate) {
		return Arrays.asList(String.valueOf(n), String.valueOf(nInterior), 
				String.valueOf(nNonEmptyLeaf),String.valueOf(nEmptyLeaf),
				String.valueOf(updates), String.valueOf(nHashesRequiredToUpdate),
				String.valueOf(totalFull),String.valueOf(totalUpdate), 
				String.valueOf(avgFull), String.valueOf(avgUpdate));
	}

	public static List<String> benchmarkProofSizes(int n, int updates) {
		System.out.println(
				"--------- starting benchmark for entries:" + n + " - updates: " + updates + " the MPT ---------");
		String startingSalt = "starting-entry";
		List<Map.Entry<byte[], byte[]>> kvpairsStart = Utils.getKeyValuePairs(n, startingSalt);
		System.out.println("--------- building the MPT ---------");
		MPTDictionaryFull mpt = Utils.makeMPTDictionaryFull(kvpairsStart);
		
		int nInteriorNodes = mpt.countInteriorNodes();
		int nNonEmptyLeafNodes = mpt.countNonEmptyLeafNodes();
		int nEmptyLeafNodes = mpt.countEmptyLeafNodes();
		
		
		byte[] commitment = mpt.commitment();
		System.out.println("--------- calculating commitment ---------");
		System.out.println("--------- new commitment: " + Utils.byteArrayAsHexString(commitment) + " ---------");
		System.out.println("--------- making updates ---------");
		List<byte[]> keys = kvpairsStart.stream().map(x -> x.getKey()).collect(Collectors.toList());
		Collections.shuffle(keys);
		// select some random keys to update
		List<byte[]> keysToUpdate = keys.subList(0, updates);
		int i = 0;
		// this is just some new salt to generate a new value
		String newSalt = "new salt";
		for (byte[] key : keysToUpdate) {
			byte[] newValue = Utils.getValue(i, newSalt);
			mpt.insert(key, newValue);
		}
		System.out.println("--------- updates done, making delta and calculating new commitment ---------");
		
		int hashesRequiredToUpdate = mpt.countHashesRequiredToCommit();
		
		commitment = mpt.commitment();
		MPTDictionaryDelta delta = new MPTDictionaryDelta(mpt);
		System.out.println(Utils.byteArrayAsHexString(commitment));
		System.out.println("--------- calculating proof sizes ---------");
		BigInteger totalFull = BigInteger.ZERO;
		BigInteger totalUpdate = BigInteger.ZERO;
		for (byte[] key : keys) {
			MPTDictionaryPartial fullPath = new MPTDictionaryPartial(mpt, key);
			int fullPathSerialziationSize = fullPath.serialize().toByteArray().length;
			int onlyUpdateSerializationSize = delta.getUpdates(key).toByteArray().length;
			totalFull = totalFull.add(BigInteger.valueOf(fullPathSerialziationSize));
			totalUpdate = totalUpdate.add(BigInteger.valueOf(onlyUpdateSerializationSize));
		}
		BigInteger avgUpdate = totalUpdate.divide(BigInteger.valueOf(keys.size()));
		BigInteger avgFull = totalFull.divide(BigInteger.valueOf(keys.size()));
		System.out.println("total full : " + totalFull + " | total update : " + totalUpdate);
		System.out.println("average full : " + avgFull + " | average update : " + avgUpdate);
		return getCSVRow(n, nInteriorNodes, nNonEmptyLeafNodes, nEmptyLeafNodes, 
				updates, hashesRequiredToUpdate, totalFull, totalUpdate, avgFull, avgUpdate);
	}
	
	public static List<List<String>> runExperiment(int n, List<Double> fractionsToUpdate){
		List<List<String>> experimentResults = new ArrayList<>();
		for(double fractionToUpdate : fractionsToUpdate) {
			int updates = (int) (n*fractionToUpdate);
			// run an experiment for each fraction of updates and 
			// calculate the results
			List<String> experimentResult = benchmarkProofSizes(n, updates);
			experimentResults.add(experimentResult);
		}
		return experimentResults;
	}
	
	public static void writeResultsToCSV(List<List<String>> results, String csvFile) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile));
				CSVPrinter csvPrinter = new CSVPrinter(writer, 
						CSVFormat.DEFAULT.withHeader("n", "updates",
								"total_full_path", "total_update_path", 
								"full_path_avg", "update_path_avg"));) {
			for(List<String> resultRow : results) {
				csvPrinter.printRecord(resultRow);
			}
			csvPrinter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String[] args) {
		int n = 1000000;
		List<Double> fractionsToTest = Arrays.asList(0.001, 0.005, 0.01, 0.02, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5);
		List<List<String>> results = runExperiment(n, fractionsToTest);
		writeResultsToCSV(results, "./benchmark-results.csv");
	}

}
