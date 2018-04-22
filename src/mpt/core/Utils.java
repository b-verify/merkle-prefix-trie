package mpt.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpt.dictionary.MPTDictionaryFull;
import mpt.set.MPTSetFull;

public class Utils {
	
	public static String getKeyString(int i) {
		return "key"+Integer.toString(i);
	}
	
	public static String getValueString(int i, String salt) {
		return "value"+Integer.toString(i)+salt;
	}
	
	public static MPTDictionaryFull makeMPTDictionaryFull(int numberOfEntries, String salt) {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		for(int i = 0; i < numberOfEntries; i++) {
			String keyString = Utils.getKeyString(i);
			String valueString = Utils.getValueString(i, salt);
			if(i % 1000 == 0) {
				System.out.println("made "+(i+1)+" of "+numberOfEntries);
			}
			mpt.insert(keyString.getBytes(), valueString.getBytes());
		}
		return mpt;
	}
		
	public static List<Map.Entry<String, String>> getKeyValuePairs(int numberOfEntries, String salt){
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>();
		for(int key = 0; key < numberOfEntries; key++) {
			String keyString = Utils.getKeyString(key);
			String valueString = Utils.getValueString(key, salt);
			list.add(Map.entry(keyString, valueString));
		}
		return list;
	}
	
	public static MPTDictionaryFull makeMPTDictionaryFull(List<Map.Entry<String, String>> kvpairs) {
		MPTDictionaryFull mpt = new MPTDictionaryFull();
		int i = 1;
		for(Map.Entry<String, String> kvpair : kvpairs) {
			if(i % 1000 == 0) {
				System.out.println("made "+i+" of "+kvpairs.size());
			}
			mpt.insert(kvpair.getKey().getBytes(), kvpair.getValue().getBytes());
			i++;
		}
		return mpt;
	}
	
	public static MPTSetFull makeMPTSetFull(int numberOfEntries, String salt) {
		MPTSetFull mpt = new MPTSetFull();
		for(int i = 0; i < numberOfEntries; i++) {
			String valueString = Utils.getValueString(i, salt);
			if(i % 1000 == 0) {
				System.out.println("made "+(i+1)+" of "+numberOfEntries);
			}
			mpt.insert(valueString.getBytes());
		}
		return mpt;
	}
	
	public static List<String> getValues(int numberOfEntries, String salt){
		List<String> list = new ArrayList<>();
		for(int i = 0; i < numberOfEntries; i++) {
			String valueString = Utils.getValueString(i, salt);
			list.add(valueString);
		}
		return list;
	}
	
	public static MPTSetFull makeMPTSetFull(List<String> values) {
		MPTSetFull mpt = new MPTSetFull();
		int i = 1;
		for(String value: values) {
			if(i % 1000 == 0) {
				System.out.println("made "+i+" of "+values.size());
			}
			mpt.insert(value.getBytes());
			i++;
		}
		return mpt;
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

}
