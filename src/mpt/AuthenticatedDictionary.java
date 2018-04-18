package mpt;

import java.util.List;
import java.util.Map;

/**
 * A Persistent Authenticated Dictionary (PAD). A PAD 
 * provides the abstraction of a key-value store. 
 * Each dictionary consists of a set of (key, value) pairs
 * and is deterministically mapped to a small crpytographic commitment.
 * Individual lookups can be authenticated 
 * against this commditment.
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public interface AuthenticatedDictionary {
	
	/**
	 * 		 	METHODS PRIMARILY USED ON SERVER
	 */
		
	/**
	 * Inserts a (key,value) mapping into the dictionary. 
	 * If the key is mapped to some other value then 
	 * the current mapping is overwritten. The authentication 
	 * information is immediately updated. 
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @param value - arbitrary bytes representing the value
	 * @return true if the (key, value) mapping was not previously in the 
	 * dictionary
	 * @throws IncompleteMTPException - if there is insufficient information
	 * to insert the value
	 */
	public boolean insert(byte[] key, byte[] value) throws IncompleteMPTException;
	
	/**
	 * Insert a batch consisting of multiple (key, value) 
	 * mappings into the dictionary. Authentication information
	 * is updated efficiently - meaning that inserting 
	 * a batch of key values is much more efficient than individually
	 * inserting each mapping.
	 * @param keys
	 * @param values
	 * @throws IncompleteMPTException
	 */
	public void insertBatch(List<Map.Entry<byte[], byte[]>> kvpairs) throws IncompleteMPTException;
		
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
	 * Returns an authenticated dictionary containing only the authentication
	 * information that has changed (as a result of inserts and deletes) 
	 * since the last call to reset()
	 * @return
	 */
	public AuthenticatedDictionary getChangedNodes();
	
	/**
	 * Marks all nodes as "unchanged". This is used to reset
	 * tracking of changes to the data structure. 
	 */
	public void reset();
	
	/**
	 * Returns a serialized update that allows for the authentication 
	 * authentication path for the specified keys to be updated.
	 * @param keys - a list of keys for which to provide authentication
	 * path updates.
	 * @return
	 */
	public byte[] getUpdates(List<byte[]> keys);
		
	/**
	 * Copies the (key, value) mapping from the other authenticated
	 * dictionary into this dictionary along with 
	 * the authentication information from other.
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
	 * 		 	METHODS PRIMARILY USED ON CLIENT
	 */
	
	/**
	 * Returns a cryptographic commitment to the set of (key,value) 
	 * mappings contained in this dictionary.
	 * @return
	 */
	public byte[] commitment();

	/**
	 * Get the value in the (key,value) mapping in the dictionary.
	 * If the key is not in the dictionary, returns null.
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @return value if (key,value) is in the tree and null if key is not 
	 * in the dictionary
	 * @throws IncompleteMPTException if there is insufficient information
	 * to determine if a key is present or absent
	 */
	public byte[] get(byte[] key) throws IncompleteMPTException;
	
	/**
	 * @christina - yeah this should really be a static method... not sure how
	 * we should handle this 
	 * 
	 * Deserialize an authenticated dictionary from bytes
	 * @param dicitionaryAsBytes - a serialization of an authenticated dictionary
	 * @throws InvalidMPTSerializationException if the data cannot properly be 
	 * deserialized
	 * @return
	 */
	public void deserialize(byte[] dicitionaryAsBytes) throws InvalidMPTSerializationException;
	
	/**
	 * Deserialize an update to an authenticated dictionary from bytes. This
	 * consists of the information required to update the authentication
	 * paths contained in this dictionary. Note that Updates
	 * change the authentication information so the dictionary may have a new
	 * commitment!
	 * @param updateBytes - a byte representation of an UPDATE
	 * to this dictionary 
	 * @throws InvalidMPTSerializationException if the data cannot properly be 
	 * deserialized or if the update is invalid
	 * @return
	 */
	public void deserializeUpdates(byte[] updateBytes) throws InvalidMPTSerializationException;
	
}
