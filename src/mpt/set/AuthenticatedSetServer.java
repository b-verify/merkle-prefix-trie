package mpt.set;

/**
 * This in the interface for the  "server" version 
 * of the authenticated set. The "server" version stores
 * all of the data contained in the set along 
 * with the authentication information.
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
	 * @param value - arbitrary bytes representing a value
	 */
	public void insert(final byte[] value);
	
	/**
	 * Delete a value from the set,
	 * if it exists.
	 * @param value - arbitrary bytes representing the 
	 * value to be deleted.
	 */
	public void delete(final byte[] value);
	
	/**
	 * Returns true if a value is in the set 
	 * and false if the value is not in the set 
	 * @param value - arbitrary bytes representing 
	 * the value 
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
	 * Serialize the authenticated set 
	 * to an efficient byte representation
	 * that can be deserialized.
	 * @return
	 */
	public byte[] serialize();
		
}
