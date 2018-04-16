package crpyto;

import java.security.MessageDigest;

/**
 * 
 * @author henryaspegren
 *
 */
public class CryptographicDigest {

	private static final String HASH_FUNCTION = "SHA-256";
	private static final int SIZE_BITS = 256;
	private static final int SIZE_BYTES = SIZE_BITS / 8;
	
	public static byte[] digest(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
			byte[] digest = md.digest(input);
			return digest;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Commits to a key and a value using the following commitment 
	 * 
	 * 					H(H(key)||H(value))
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static byte[] witnessKeyAndValue(byte[] key, byte[] value) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
			byte[] digestKey = md.digest(key);
			byte[] digestValue = md.digest(value);
			byte[] witness = new byte[digestKey.length+digestValue.length];
			System.arraycopy(digestKey, 0, witness, 0, digestKey.length);
			System.arraycopy(digestValue, 0, witness, digestKey.length, digestValue.length);
			return witness;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int getSizeBits() {
		return SIZE_BITS;
	}
	
	public static int getSizeBytes() {
		return SIZE_BYTES;
	}
	
	public static String getHashFunction() {
		return HASH_FUNCTION;
	}
		
}
