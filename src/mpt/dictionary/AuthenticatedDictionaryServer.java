package mpt.dictionary;

/**
 * This is the interface the server should use for managing
 * the full Authenticated Dictionary.
 * 
 * The server stores the full Authenticated Dictionary
 * and is responsible for making changes to it. 
 * 
 * Additionally the server is responsible for constructing 
 * and updating client "partial view" of the Authenticated
 * Dictionary and creating client-specific updates
 * as the Dictionary changes
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public interface AuthenticatedDictionaryServer {

	/**
	 * Insert a (key,value) mapping into the dictionary. 
	 * If the key is currently mapped to some other value, 
	 * the value is updated. Authentication information
	 * is updated *lazily* - meaning that calculation
	 * of hashes is delayed until this.commitment()
	 * is called!
	 * 
	 * Additionally the dictionary records all insertions
	 * as changes and tracks which nodes have been changed
	 * for the purpose of calculating updates.
	 * 
	 * @param key - arbitrary bytes representing a key
	 * @param value - arbitrary bytes representing a value
	 */
	public void insert(final byte[] key, final byte[] value);
	
	/**
	 * Get the value mapped to by key or null if the 
	 * key is not mapped to anything.
	 * @param key - arbitrary bytes representing a key
	 * @return
	 */
	public byte[] get(final byte[] key);
	
	/**
	 * Remove the key and its associated mapping, 
	 * if it exists, from the dictionary.
	 * 
	 * Additionally the dictionary records all deletions
	 * as changes and tracks which nodes have been changed
	 * for the purpose of calculating updates.
	 * @param key
	 */
	public void delete(final byte[] key);
	
	/**
	 * Get a small cryptographic commitment to the authenticated 
	 * dictionary. For any given set of (key,value) mappings,
	 * regardless of the order they inserted the commitment 
	 * will be the same and it is computationally 
	 * infeasible to find a different set of (key, value) mappings 
	 * with the same commitment.
	 * @return
	 */
	public byte[] commitment();
	
	/**
	 * Resets the current state of the authenticated dictionary
	 * to have no changes. Changes all nodes
	 * currently marked as "changed" to "unchanged"
	 */
	public void reset();
	
	/**
	 * Return an efficient serialization of the 
	 * full authenticated dictionary
	 * @return
	 */
	public byte[] serialize();
	
}
