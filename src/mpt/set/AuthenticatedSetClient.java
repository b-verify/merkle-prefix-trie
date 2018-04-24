package mpt.set;

import mpt.core.InsufficientAuthenticationDataException;
import serialization.MptSerialization.MerklePrefixTrie;

/**
 * This "client" version of the authenticated set.
 * The client version contains a subset of the data 
 * in thhe set along with only the required authentication
 * information. The advantage of this is that  
 * the client version is asymptotically smaller
 * and more practical for devices with limited storage
 * or bandwidth. 
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
	 * @param value - arbitrary bytes
	 * @return
	 * @throws InsufficientAuthenticationDataException - thrown 
	 * if there is not enough authentication information to 
	 * determine if a value is in the set
	 */
	public boolean inSet(final byte[] value) throws InsufficientAuthenticationDataException;
	
	/**
	 * Returns a small cryptographic commitment to 
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
