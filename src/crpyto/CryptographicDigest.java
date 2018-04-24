package crpyto;

import java.security.MessageDigest;
import java.util.List;

/**
 * This is a wrapper that exposes the required
 * cryptographic operations. This actual implementations 
 * should be a dedicated, standard cryptographic library.
 * The underlying cryptographic library can be swapped 
 * out.
 * 
 * @author henryaspegren
 *
 */
public class CryptographicDigest {

	static final String HASH_FUNCTION = "SHA-256";
	private static final int SIZE_BITS = 256;
	private static final int SIZE_BYTES = SIZE_BITS / 8;
	
	/**
	 * Calculates the cryptographic hash of the input
	 * @param input
	 * @return
	 */
	public static byte[] hash(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
			byte[] digest = md.digest(input);
			return digest;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] hash(List<byte[]> inputs) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
			for(byte[] input : inputs) {
				md.update(input);
			}
			byte[] digest = md.digest();
			return digest;
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
