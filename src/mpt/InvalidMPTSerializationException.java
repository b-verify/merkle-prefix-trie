package mpt;

/**
 * This exception indicates that a MPT was not 
 * correctly serialized.
 * @author henryaspegren
 *
 */
public class InvalidMPTSerializationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidMPTSerializationException(String message) {
		super(message);
	}
	
}
