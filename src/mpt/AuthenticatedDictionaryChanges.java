package mpt;

import java.util.List;

/**
 * This is the interface the server should use for keeping 
 * track of changes to an Authenticated Dictionary
 * 
 * Inserts and deletes change the dictionary along with
 * the authentication information. This interface should
 * be used for tracking these changes. It allows 
 * the server to construct client-specific updates
 * to the client's view of the Authenticated Dictionary 
 * 
 * @author Henry Aspegren, Chung Eun (Christina) Lee
 *
 */
public interface AuthenticatedDictionaryChanges {

	/**
	 * Given a specific key, this method calculates 
	 * the updates that should be sent to a client 
	 * whose authenticated dictionary tracks this key. 
	 * 
	 * To reduce the size of the updates this method
	 * caches unchanged values on the client and 
	 * avoids retransmitting them.
	 * 
	 * The client can deserialize this update and 
	 * her view of the authenticated dictionary will 
	 * now reflect the update.
	 * 
	 * @param key - arbitrary bytes
	 * @return
	 */
	public byte[] getUpdates(final byte[] key);
	
	
	/**
	 * Given a set of keys, this method calculates 
	 * the updates that should be sent to a client 
	 * whose authenticated dictionary tracks these 
	 * keys. 
	 * 
	 * To reduce the size of the updates this method
	 * caches unchanged values on the client and 
	 * avoids retransmitting them.
	 * 
	 * The client can deserialize this update 
	 * and her view of the authenticated dictionary
	 * will now reflect the update.
	 * @param keys
	 * @return
	 */
	public byte[] getUpdates(final List<byte[]> keys);
}
