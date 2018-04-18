package mpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
	
	public static MerklePrefixTrie makeMerklePrefixTrie(int numberOfEntries, String salt) {
		return Utils.makeMerklePrefixTrie(
				Utils.getKeyValuePairs(numberOfEntries, salt));
	}
	
	public static List<Map.Entry<String, String>> getKeyValuePairs(int numberOfEntries, String salt){
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>();
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = "key"+Integer.toString(key);
			String valueString = "value"+Integer.toString(key)+salt;
			list.add(Map.entry(keyString, valueString));
		}
		return list;
	}
	
	public static MerklePrefixTrie makeMerklePrefixTrie(List<Map.Entry<String, String>> kvpairs) {
		try {
			MerklePrefixTrie mpt = new MerklePrefixTrie();
			int i = 1;
			for(Map.Entry<String, String> kvpair : kvpairs) {
				if(i % 1000 == 0) {
					System.out.println("made "+i+" of "+kvpairs.size());
				}
				mpt.insert(kvpair.getKey().getBytes(), kvpair.getValue().getBytes());
				i++;
			}
			return mpt;
		}catch( IncompleteMPTException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Get the bit at index in a byte array. 
	 * byte array:   byte[0]|| byte[1] || byte[2]  || byte[3]
	 * index		 [0...7]  [8...15]   [16...23]    [24...31]
	 * @param bytes array of bytes representing a single value (byte[0]||byte[1]||..)
	 * @param index the index of the bit
	 * @return true if the bit is 1 and false if the bit is 0
	 */
	public static boolean getBit(final byte[] bytes, int index) {
		int byteIndex = Math.floorDiv(index, 8); 
		//int bitIndex = index % 8;
		int bitIndex = (7 - index) % 8;
		if (bitIndex < 0) {
			bitIndex += 8;
		}
		byte b = bytes[byteIndex];
		return Utils.getBit(b, bitIndex);
	}
	
	/**
	 * Get the index'th bit in a byte 
	 * @param b a byte
	 * @param index index of the bit to get in [0, 7] (0-indexed)
	 * @return
	 */
	public static boolean getBit(final byte b, int index) {
		switch(index) {
		case 0:
			return (b & 1) != 0;
		case 1:
			return (b & 2) != 0;
		case 2:
			return (b & 4) != 0;
		case 3:
			return (b & 8) != 0;
		case 4:
			return (b & 0x10) != 0;
		case 5:
			return (b & 0x20) != 0;
		case 6:
			return (b & 0x40) != 0;
		case 7:
			return (b & 0x80) != 0;
		}
		throw new RuntimeException("Only 8 bits in a byte - bit index must between 0 and 7");
	}
	
	/**
	 * Return an array of bytes as string of bits
	 * @param bytes
	 * @return
	 */
	public static String byteArrayAsBitString(final byte[] bytes) {
		String bitString = "";
		for(byte b: bytes) {
			for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
				if(Utils.getBit(b, bitIndex)) {
					bitString += "1";
				}else {
					bitString += "0";
				}
			}
		}
		return bitString;
	}
	
	/**
	 * Print a byte array as a human readable hex string
	 * @param raw
	 * @return
	 */
	public static String byteArrayAsHexString(final byte[] raw) {
	    final StringBuilder hex = new StringBuilder(2 * raw.length);
	    for (final byte b : raw) {
	        hex.append(Integer.toHexString(b));
	    }
	    return hex.toString();
	}
	
	/**
	 * Print a prefix as a bit string
	 * @param bytes - bytes representing the "full path" 
	 * @param endIdx - an int such that prefix = bytes[:endIdx]
	 * @return
	 */
	public static String byteArrayPrefixAsBitString(final byte[] bytes, int endIdx) {
		String bitString = "";
		int idx = 0;
		for(byte b: bytes) {
			for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
				if(Utils.getBit(b, bitIndex)) {
					bitString += "1";
				}else {
					bitString += "0";
				}
				if(idx == endIdx) {
					return bitString;
				}
				idx++;
			}
		}
		return bitString;
	}
	
	/**
	 * Get the overlapping prefix between two byte sequences. 
	 * This is defined to be the first index i that the two byte
	 * sequences diverge at byteSeqA[i] != Utils.getBit(byteSeqB, i)
	 * @param byteSeqA
	 * @param byteSeqB
	 * @return an integer >= 0 representing the overlapping prefix index
	 */
	public static int getOverlappingPrefix(final byte[] byteSeqA, final byte[] byteSeqB) {
		assert byteSeqA.length == byteSeqB.length;
		int idx = 0;
		for(int byteIdx = 0; byteIdx < byteSeqA.length; byteIdx++) {
			for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
				boolean bitA = Utils.getBit(byteSeqA[byteIdx], bitIndex);
				boolean bitB = Utils.getBit(byteSeqB[byteIdx], bitIndex);
				if(bitA != bitB) {
					return idx;
				}
				idx++;
			}
		}
		return idx;
	}
	

}
