package mpt;

/**
 * A Persistent Authenticated Dictionary (PAD). A PAD 
 * provides a key-value store. Each set of (key, value) mappings
 * is deterministically mapped to a small crpytographic commitment.
 * This commitment can be used to authenticate queries
 * against the data stored in the PAD.
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public interface AuthenticatedDictionary {
	
	/** constructor: creates an EMPTY dictionary {} */
	
	/**
	 * Inserts a (key,value) mapping into the dictionary. 
	 * If the key is already present in the dictionary 
	 * and mapped to this value or some other value the 
	 * method will not add (key,value) and will
	 * return false.
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return true iff the (key, value) mapping was added
	 */
	public boolean insert(byte[] key, byte[] value);
		
	/**
	 * Removes any mapping associated with key from the dictionary.
	 * If the key is not present in the dictionary nothing is 
	 * modified and this method returns false.
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @return true iff the key was present in the dictionary
	 */
	public boolean delete(byte[] key);
	
	/**
	 * Updates the (key, value) mapping in the dictionary and
	 * returns true if the dictionary contains
	 * the (key, value) mapping. If the key is not present 
	 * in the dictionary will return false.
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return true iff the (key,value) mapping is in the dictionary
	 * @throws IncompleteMPTException - if there is not enough information
	 * in the underlying data structure to successfully update the mapping
	 */
	public boolean update(byte[] key, byte[] value) throws IncompleteMPTException;
	
	/**
	 * Get the value in the (key,value) mapping in the dictionary.
	 * If the key is not in the dictionary, returns null.
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @return value if (key,value) is in the tree and null if key is not 
	 * in the PAD
	 * @throws IncompleteMPTException if there is insufficient information
	 * to decid
	 */
	public byte[] get(byte[] key) throws IncompleteMPTException;
	
	/**
	 * Returns a cryptographic commitment to the set of (key,value) 
	 * mappings contained in this dictionary.
	 * @return
	 */
	public byte[] commitment();
	
	/**
	 * Copies the (key, value) mapping from the other authenticated
	 * dictionary into this dictionary along with 
	 * the authentication information from the other.
	 * 
	 * @param other - another authenticated dictionary from which we wish 
	 * to copy a mapping along with the associated authentication information
	 * @param key - the mapping to copy - NOTE the key may not be present. 
	 * Authenticated dictionaries allow for proof of membership and of 
	 * non-membership. This proof is copied from the other into this 
	 * authenticated dictionary
	 * @return true if the copy was successful
	 */
	public boolean copyAuthenticatedKey(AuthenticatedDictionary other, byte[] key);
	
	/**
	 * Serialize this authenticated dictionary to bytes
	 * @return
	 */
	public byte[] serialize();
	
	/**
	 * Deserialize an authenticated dictionary from bytes
	 * @param trieasbytes - a byte representation of the trie
	 * @throws InvalidMPTSerializationException if the data cannot properly be 
	 * deserialized
	 * @return
	 */
	public void deserialize(byte[] trieasbytes) throws InvalidMPTSerializationException;
	
	/**
	 * Deserialize an update to an authenticated dictionary from bytes
	 * @param updateBytes - a byte representation of an UPDATE
	 * to the trie
	 * @throws InvalidMPTSerializationException if the data cannot properly be 
	 * deserialized or if the update is invalid
	 * @return
	 */
	public void deserializeUpdates(byte[] updateBytes) throws InvalidMPTSerializationException;
	
}
