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
