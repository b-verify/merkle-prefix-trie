package mpt;

/**
 * This exception indicates that a MPT was not 
 * correctly serialized.
 * @author henryaspegren
 *
 */
public class InvalidMPTSerialization extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidMPTSerialization(String message) {
		super(message);
	}
	
}
