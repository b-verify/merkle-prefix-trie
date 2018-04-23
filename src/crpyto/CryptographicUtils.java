package crpyto;

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
	 * 					H(H(key)||H(value))
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static byte[] witnessKeyAndValue(byte[] key, byte[] value) {
		byte[] digestKey = CryptographicDigest.hash(key);
		byte[] digestValue = CryptographicDigest.hash(value);
		byte[] witnessPreImage = new byte[digestKey.length+digestValue.length];
		System.arraycopy(digestKey, 0, witnessPreImage, 0, digestKey.length);
		System.arraycopy(digestValue, 0, witnessPreImage, digestKey.length, digestValue.length);
		byte[] witness = CryptographicDigest.hash(witnessPreImage);
		return witness;
	}

}
