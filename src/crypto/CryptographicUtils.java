package crypto;

/**
 * This class contains the various cryptographic 
 * commitments used by the rest of the library
 * @author henryaspegren
 *
 */
public class CryptographicUtils {

	/**
	 * Commits to a key and a value using the following commitment 
	 * 
	 * 					H(key||value)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static byte[] witnessKeyAndValue(byte[] key, byte[] value) {
		byte[] witnessPreImage = new byte[key.length+value.length];
		System.arraycopy(key, 0, witnessPreImage, 0, key.length);
		System.arraycopy(value, 0, witnessPreImage, key.length, value.length);
		byte[] witness = CryptographicDigest.hash(witnessPreImage);
		return witness;
	}

}
