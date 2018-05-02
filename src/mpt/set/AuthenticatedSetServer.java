package mpt.set;

import serialization.generated.MptSerialization.MerklePrefixTrie;

/**
 * This in the interface for the  "server" version 
 * of the authenticated set. The "server" version stores
 * all of the data contained in the set along 
 * with the authentication information.
 * 
 * NOTE: the current implementation defines 
 * values to be fixed length byte-arrays and 
 * relies on the user of this library to ensure that this 
 * is the case. The easiest way to do this is to use
 * a cryptographic hash function to commit to 
 * the larger values. The larger values
 * can be provided and authenticated with just the 
 * hash values. The reasoning for this choice is 
 * that it keeps the size of the data structure and
 * the messages exchanged as as small as possible.
 * Larger values can be stored elsewhere.
 * 
 * An authenticated set is a set of elements and allows simple 
 * proofs of membership and non-membership. Elements 
 * can be added and removed.
 * 
 * 
 * @author henryaspegren
 *
 */
public interface AuthenticatedSetServer {

	/**
	 * Insert a value into the set
	 * @param value - a fixed length byte array (e.g. the output of a hash)
	 * representing a value to be inserted
	 */
	public void insert(final byte[] value);
	
	/**
	 * Delete a value from the set,
	 * if it exists.
	 * @param value - a fixed length byte array (e.g. the output of a hash)
	 * representing a value to be deleted.
	 */
	public void delete(final byte[] value);
	
	/**
	 * Returns true if a value is in the set 
	 * and false if the value is not in the set 
	 * @param value - a fixed length byte array (e.g. the output of a hash)
	 * representing a value
	 * @return
	 */
	public boolean inSet(final byte[] value);
	
	/**
	 * Returns a short cryptographic commitment 
	 * to the set of values. It is computationally 
	 * difficult to find two sets that map to the 
	 * same commitment.
	 * @return
	 */
	public byte[] commitment();
	
	/**
	 * Returns a protobuf serialization of this 
	 * data structure that can easily be 
	 * converted to bytes and sent on the wire.
	 * @return
	 */
	public MerklePrefixTrie serialize();
		
}
