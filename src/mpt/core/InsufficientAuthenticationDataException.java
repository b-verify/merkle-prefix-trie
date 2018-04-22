package mpt.core;

/**
 * This exception indicates that an operation in an ADS
 * required access to authentication information not 
 * currently present. 
 * 
 * For example if in searching a MPT if the search attempts
 * to proceed down a path without the necessary authentication
 * information (nodes on the co-path), this exception 
 * can be thrown to alert the client. 
 * 
 * The key distinction here is that this exception indicates
 * that the user does not currently have enough authentication
 * information to execute the operation.
 * 
 * @author henryaspegren
 *
 */
public class InsufficientAuthenticationDataException extends Exception {

	private static final long serialVersionUID = 1L;

	public InsufficientAuthenticationDataException(String message) {
		super(message);
	}
	
}
