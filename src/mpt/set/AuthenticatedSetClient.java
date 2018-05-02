package mpt.set;

import mpt.core.InsufficientAuthenticationDataException;
import serialization.generated.MptSerialization.MerklePrefixTrie;

/**
 * This "client" version of the authenticated set.
 * The client version contains a subset of the data 
 * in the set along with only the required authentication
 * information. The advantage of this is that  
 * the client version is asymptotically smaller
 * and more practical for devices with limited storage
 * or bandwidth. 
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
 * @author henryaspegren
 *
 */
public interface AuthenticatedSetClient {
	
	/**
	 * Returns true if a value is in the set and false if it is not. 
	 * Throws an exception if there is not enough 
	 * authentication information to determine if a value is 
	 * in a set
	 * @param value - a fixed length byte array (e.g. the output of a hash)
	 * representing a value
	 * @return
	 * @throws InsufficientAuthenticationDataException - thrown 
	 * if there is not enough authentication information to 
	 * determine if a value is in the set
	 */
	public boolean inSet(final byte[] value) throws InsufficientAuthenticationDataException;
	
	/**
	 * Returns a fixed length cryptographic commitment to 
	 * the set. It is computationally hard to find 
	 * two sets with distinct values that have the
	 * same commitments. 
	 * @return
	 */
	public byte[] commitment();

	/**
	 * Returns a protobuf serialization of this 
	 * data structure that can easily be 
	 * converted to bytes and sent on the wire.
	 * @return
	 */
	MerklePrefixTrie serialize();
}
