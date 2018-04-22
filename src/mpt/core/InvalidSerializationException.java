package mpt.core;

/**
 * This exception indicates that a MPT was not 
 * correctly serialized.
 * @author henryaspegren
 *
 */
public class InvalidSerializationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidSerializationException(String message) {
		super(message);
	}
	
}
