package crpyto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author henryaspegren
 *
 */
public class CryptographicDigest {

	private static final String HASH_FUNCTION = "SHA-256";
	private static final int SIZE_BITS = 256;
	private static final int SIZE_BYTES = SIZE_BITS / 8;
	
	public static byte[] digest(byte[] input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
		byte[] digest = md.digest(input);
		return digest;
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
