package mpt.dictionary;

import mpt.core.InsufficientAuthenticationDataException;
import mpt.core.InvalidSerializationException;
import serialization.MptSerialization;

/**
 * This is the interface the client should rely on for using an
 * Authenticated Dictionary. 
 * 
 * Clients do not need to store the entire dictionary. Instead 
 * they store some subset of entries along with the 
 * necessary authentication information. 
 * 
 * The server managing the full ADS takes care to 
 * send client-specific updates that reflect changes
 * to the ADS. 
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public interface AuthenticatedDictionaryClient {

	/**
	 * Get a (key, value) mapping from the authenticated dictionary.
	 * This method looks up the key in the dictionary and returns the associated 
	 * value if it exists and null if the key is not 
	 * mapped to any value. 
	 * 
	 * @param key - arbitrary bytes representing the key
	 * @return value bytes, if a key is mapped to a value and null otherwise
	 * @throws InsufficientAuthenticationDataException - if there is not 
	 * enough authentication information to determine if the key is mapped to a value
	 * or which value it is mapped to this exception is thrown. This may
	 * occur because the client does not have the full set of mappings
	 * in the authenticated dictioanry
	 */
	public byte[] get(final byte[] key) throws InsufficientAuthenticationDataException;
	
	/**
	 * Returns a short cryptographic commitment to the entire set of 
	 * (key,value) mappings in the full authenticated dictionary.
	 * @return
	 */
	public byte[] commitment();
	
	/**
	 * Updates the authenticated dictionary to reflect changes. This 
	 * will change the commitment as mappings have been inserted or removed
	 * @param updates - a protobuf representation of this update (which 
	 * can be serialized and deserialized)
	 * @throws InvalidSerializationException - thrown if the update cannot
	 * be performed.
	 */
	public void processUpdates(MptSerialization.MerklePrefixTrie updates) throws InvalidSerializationException;
	
	/**
	 * Serialize the authenticated dictionary as a concise byte representation
	 * @return
	 */
	public byte[] serialize();
	
}
